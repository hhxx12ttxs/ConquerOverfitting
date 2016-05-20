package com.kivancmuslu.www.utils.git;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jgit.api.CherryPickCommand;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.CherryPickResult.CherryPickStatus;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.CannotDeleteCurrentBranchException;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.NotMergedException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialItem.Password;
import org.eclipse.jgit.transport.CredentialItem.Username;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import com.kivancmuslu.www.debug.Debug;
import com.kivancmuslu.www.nullness.StringUtil;
import com.kivancmuslu.www.system.SystemConstants;
import com.kivancmuslu.www.utils.git.nullness.StatusUtil;

public class GitOperations implements SystemConstants
{
    // Below are defined just in case we need them in the future.
    @SuppressWarnings("unused") private static final String SIZE_OF_PACKED_OBJECTS = "sizeOfPackedObjects";
    @SuppressWarnings("unused") private static final String SIZE_OF_LOOSE_OBJECTS = "sizeOfLooseObjects";
    @SuppressWarnings("unused") private static final String NO_PACKED_OBJECTS = "numberOfPackedObjects";
    @SuppressWarnings("unused") private static final String NO_PACKED_FILES = "numberOfPackedFiles";
    @SuppressWarnings("unused") private static final String NO_PACKED_REFS = "numberOfPackedRefs";
    @SuppressWarnings("unused") private static final String NO_LOOSE_REFS = "numberOfLooseRefs";
    private static final String NO_LOOSE_OBJECTS = "numberOfLooseObjects";

    public static final String MASTER_BRANCH_NAME = "master";
    public static final String SQUASH_PREFIX = "Included revisions: ";

    private static final boolean DEBUG_CLEAN = false;

    /**
     * Initializes the repository if necessary. Equivalent to:
     * 
     * <pre>
     * # if (repository does not exist)
     * > git init
     * </pre>
     */
    public static GitOperations initRepositoryIfNecessary(File repository) throws GitAPIException,
        IOException
    {
        File gitFolder = getGitFolder(repository);
        if (!gitFolder.exists())
            return initRepository(repository);
        return getGit(repository);
    }

    /**
     * Initializes the repository. Equivalent to:
     * 
     * <pre>
     * > git init
     * </pre>
     */
    public static GitOperations initRepository(File repository) throws GitAPIException, IOException
    {
        Git.init().setDirectory(repository).call();
        return getGit(repository);
    }

    public static boolean isGitRepository(File file)
    {
        if (!file.exists())
            return false;
        return RepositoryCache.FileKey.isGitRepository(file, org.eclipse.jgit.util.FS.DETECTED);
    }

    // Searches up through the directory tree until a repository is found.
    public static GitOperations getGit(File repository) throws IOException
    {
        File gitFolder = getGitFolder(repository);

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder.setGitDir(gitFolder).readEnvironment().findGitDir().build();
        // if (repo.getGitDir() == null) -> Unnsuccessful
        return new GitOperations(new Git(repo));
    }

    private static File getGitFolder(File repository)
    {
        return new File(repository, ".git");
    }

    private final Git git_;

    public GitOperations(Git git)
    {
        git_ = git;
    }

    public DiffFormatter getDiffFormatter(OutputStream out)
    {
        DiffFormatter df = new DiffFormatter(out); // line data written to the "out" stream
        df.setRepository(getRepository());
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        return df;
    }

    /**
     * Provides the diffs between the two working trees.
     * 
     * @throws IOException
     */
    public List<DiffEntry> getDiffEntries(RevCommit parent, RevCommit child) throws IOException
    {
        RevTree parentTree = parent.getTree();
        RevTree childTree = child.getTree();
        DiffFormatter df = null;
        List<DiffEntry> diffEntries = null;
        @SuppressWarnings("null") @NonNull OutputStream outputStream = DisabledOutputStream.INSTANCE;
        df = getDiffFormatter(outputStream);
        @SuppressWarnings("null") @NonNull List<DiffEntry> safeDiffEntries = df.scan(parentTree,
                                                                                     childTree);
        diffEntries = safeDiffEntries;
        return diffEntries;
    }

    private Repository getRepository()
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Repository result = git_.getRepository();
        return result;
    }

    /**
     * Provides the diffs between a the commit {@code commit} and the working tree. Using FGHF, the
     * diff lists are expected to have length 1, unless it is a refactoring diff. Equivalent to:
     * 
     * <pre>
     * > git diff {@code commit}^ {@code commit}
     * </pre>
     */
    public List<DiffEntry> getDiffsFromCommit(RevCommit commit) throws RevisionSyntaxException,
        AmbiguousObjectException, IncorrectObjectTypeException, IOException, GitAPIException
    {
        String oldHash = commit.getParent(0).getName();
        String newHash = commit.getName();
        ObjectId old = getRepository().resolve(oldHash + "^{tree}");
//      Once you have the tree ids you can create the tree iterators and get the diffs:
//      and for the revision id:
        ObjectId head = getRepository().resolve(newHash + "^{tree}");

        ObjectReader reader = getRepository().newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        oldTreeIter.reset(reader, old);
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        newTreeIter.reset(reader, head);
        @SuppressWarnings("null") @NonNull List<DiffEntry> diffs = git_.diff()
                                                                       .setNewTree(newTreeIter)
                                                                       .setOldTree(oldTreeIter)
                                                                       .call();
        return diffs;
    }

    /**
     * Returns a String representing the diff between the commit's ancestor and the commit.
     * Equivalent to:
     * 
     * <pre>
     * > git diff {@code commit}^ {@code commit}
     * </pre>
     */
    public String getAdjacentDiff(RevCommit commit) throws IOException, RevisionSyntaxException,
        GitAPIException
    {
        String result = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream())
        {
            DiffFormatter df = getDiffFormatter(out);
            List<DiffEntry> diffs = getDiffsFromCommit(commit);
            result = "";
            for (DiffEntry diff: diffs)
            {
                df.setContext(0); // Only output the changed/removed lines.
                df.format(diff);
                df.setContext(0); // Only output the changed/removed lines.
                result += out.toString() + "\n";
                out.reset();
            }
        }
        finally
        {
            if (result == null)
                throw new RuntimeException("GitOperations.getStringFromCommit: result is null.");
        }
        return result;
    }

    /**
     * Returns the first commit after the root. Equivalent to:
     * 
     * <pre>
     * > git show HEAD
     * # where the RevCommit object is returned instead of 
     * # printing the commit information.
     * </pre>
     */
    public @Nullable RevCommit getFirstCommit()
    {
        return getCommitAtIndex(0);
    }

    /**
     * Gets the RevCommit at the given index. Indexed at 0. Returns null if the repository cannot be
     * resolved or the rev walker cannot be parsed. Equivalent to:
     * 
     * <pre>
     * > git show HEAD~{@code index}
     * # where the RevCommit object is returned instead of 
     * # printing the commit information.
     * </pre>
     */
    public @Nullable RevCommit getCommitAtIndex(int index)
    {
        RevWalk revWalker = new RevWalk(getRepository());
        RevCommit currentCommit = null;
        try
        {
            RevCommit root = getHeadCommit();
            revWalker.sort(RevSort.REVERSE);
            revWalker.markStart(root);
            for (int i = 0; i <= index; i++)
                currentCommit = revWalker.next();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return currentCommit;
    }

    // Cherrypicks the commit onto our current git branch, setting our commit name to the given
    // message.
    public CherryPickResult cherryPick(RevCommit commit, String message) throws NoMessageException,
        UnmergedPathsException, ConcurrentRefUpdateException, WrongRepositoryStateException,
        NoHeadException, GitAPIException
    {
        // Recreate the cherry pick command, adding all commits from commit list to the cherry
        // picker.
        CherryPickCommand cp = git_.cherryPick();
        cp.include(commit);

        // According the JGit API, the result cannot be null.
        @SuppressWarnings("null") @NonNull CherryPickResult commandResult = cp.setOurCommitName(message)
                                                                              .call();
        return commandResult;
    }

    /**
     * Cherrypicks the list of commits onto the current branch. Just like with Git's CherryPick, the
     * selected commits to be cherrypicked {@code commitList} must be compatible (chronological
     * order is sufficient) to avoid failure. All commit messages for the cherry-picked commits will
     * be retained. This is equivalent to:
     * 
     * <pre>
     * # {@code commitList} would be the list of commits' unique or full portion of 
     * the SHA1-ID's, from list start to end, separated by a space.
     * > git cherrypick {@code commitList}
     * 
     * </pre>
     */
    public CherryPickResult cherryPick(List<RevCommit> commitList)
        throws NoMessageException, UnmergedPathsException, ConcurrentRefUpdateException,
        WrongRepositoryStateException, NoHeadException, GitAPIException
    {
//        if (commitList.size() == 0)
//            return new CherryPickResult();

        // Recreate the cherry pick command, adding all commits from commit list to the cherry
        // picker.
        CherryPickCommand cp = git_.cherryPick();
        for (RevCommit commit: commitList)
            cp.include(commit);

        CherryPickResult commandResult = cp.call();

        if (commandResult.getStatus() != CherryPickStatus.OK)
        {
            throw new WrongRepositoryStateException(
                                               "Cherrypick status is : "
                                                   + commandResult.getStatus()
                                                   + ", which is often caused by cherry-picking commits "
                                                   + "not in chronological order.");
        }
        return commandResult;
    }

    /**
     * Squashes the commits in the current repository from the current commit until the commit
     * containing the SHA1 ID provided {@code noUpstream}. The commit referenced by the id is
     * exclusive to the squash, and will thus be the parent of the squashed commit. Equivalent to:
     * 
     * <pre>
     * > git rebase -i HEAD~{@code noUpstream}
     * 
     * # selecting 's' at the following prompt in the pop-up editor:
     * 
     * # Rebase 60709da..30e0ccb onto 60709da
     * #
     * # Commands:
     * #  p, pick = use commit
     * #  e, edit = use commit, but stop for amending
     * #  s, squash = use commit, but meld into previous commit
     * #
     * # If you remove a line here THAT COMMIT WILL BE LOST.
     * # However, if you remove everything, the rebase will be aborted.
     * #
     * # If repository is clean, return.
     * > s
     * 
     * # Then adding the message {@code message} as the squashed commit's message
     * # in the second pop-up editor:
     * 
     * # This is a combination of {@code noUpstream} commits.
     * # The first commit's message is:
     * > {@code message}
     * 
     * </pre>
     * 
     * @throws IOException
     * @throws IncorrectObjectTypeException
     * @throws AmbiguousObjectException
     * @throws RevisionSyntaxException
     */
    public void squash(String squashStartCommitId, String message) throws NoHeadException,
        WrongRepositoryStateException, GitAPIException, RevisionSyntaxException,
        AmbiguousObjectException, IncorrectObjectTypeException, IOException
    {
        @SuppressWarnings("null") @NonNull String endCommitID = getHeadCommit().getName();
        squash(squashStartCommitId, endCommitID, message);
    }

    /**
     * Assumes that we are already on the branch containing the commits that we want to squash.
     * Assumes that <@code startCommitId> has a parent commit. Squashes all commits between <@code
     * startCommitId> and <@code endCommitId>, inclusive ie. [<@code startCommitId>, <@code
     * endCommitId>]. Since the commit ID is the result of the commit itself and its parent, the
     * commit ID's of all downstream commits after the last squashed commit will be changed.
     * Equivalent to:
     * 
     * <pre>
     *  > git checkout -b temporary-branch {@code endCommitId} 
     *  > git reset {@code getCommit(startCommitId).getParent(0)} 
     *  > git add . && git commit -am "{@code message} 
     *  > git cherry-pick {@code endCommitId}^...<commit ID of HEAD>
     *  > git checkout <starting branch name>
     *  > git reset temporary-branch
     *  > git branch -d temporary-branch
     * 
     * @param startCommitId
     * @param endCommitId
     * @param message
     * @throws NoHeadException
     * @throws WrongRepositoryStateException
     * @throws GitAPIException
     * @throws RevisionSyntaxException
     * @throws AmbiguousObjectException
     * @throws IncorrectObjectTypeException
     * @throws IOException
     */
    public void squash(String startCommitId, String endCommitId, String message)
        throws NoHeadException, WrongRepositoryStateException, GitAPIException,
        RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException,
        IOException
    {
        RevCommit firstCommitToBeSquashed = getCommit(startCommitId);
        String startingBranch = getCurrentBranchName();
        String newTempBranchName = "temporary-branch";
        RevCommit lastCommitToBeSquashed = getCommit(endCommitId);

        // Obtain a stack of the downstream commits - used for cherry-picking back
        // on to the starting branch.
        Iterator<RevCommit> remainingCommitsIterator = log(lastCommitToBeSquashed.toObjectId(),
                                                           getHeadCommit().toObjectId()).iterator();
        List<RevCommit> remainingCommitsList = new ArrayList<>();
        while (remainingCommitsIterator.hasNext())
            remainingCommitsList.add(remainingCommitsIterator.next());
        Collections.reverse(remainingCommitsList); // iterator is order with newest commits first

        checkoutAsNewBranch(lastCommitToBeSquashed, newTempBranchName);
        RevCommit nonInclusiveStartCommitToBeSquashed = firstCommitToBeSquashed.getParent(0);
        git_.reset().setRef(nonInclusiveStartCommitToBeSquashed.getName()).setMode(ResetType.MIXED)
            .call(); // Note: git reset in command-line uses MIXED as its default reset type

        git_.add().addFilepattern(".").call();
        git_.commit().setAll(true).setMessage(message).call();

        // Cherry-pick the remaining commits (if any) onto our temporary branch.
        cherryPick(remainingCommitsList);

        // Ensure that we are at one commit after the last squashed commit
        checkoutBranch(startingBranch);
        git_.reset().setRef(newTempBranchName).setMode(ResetType.MIXED).call();
        deleteBranch(newTempBranchName);
    }

    /**
     * Cherry-picks the list of commits onto the current branch, then squashes these commits into a
     * single commit. Just like with Git's CherryPick, the selected commits to be cherrypicked
     * {@code commitList} must be compatible (chronological order is sufficient) to avoid failure.
     * The resulting commit will have the SHA1-Id's of all the commits that have been successfully
     * cherry-picked. Commits that have failed to cherry-pick will not be in the message. Equivalent
     * to:
     * 
     * <pre>
     * # {@code commitList} would be the list of commits' unique or full portion of 
     * the SHA1-ID's, from list start to end, separated by a space.
     * > git cherrypick {@code commitList}
     * # then running an interactive rebase (more details in the 
     * # 'squashCommitsIntoOneCommit' method).
     * 
     * > git rebase -i HEAD~(length of {@code commitList}
     * # selecting 'squash' in the pop-up editor.
     * 
     * # Then adding the SHA1-ID of each commit as the squashed commit's message,
     * # entered in the second pop-up editor.
     * 
     * </pre>
     */
    public @Nullable RevCommit cherryPickAndSquash(List<RevCommit> commitList,
                                                   boolean keepCommitMessages,
                                                   boolean includeCommitIds) throws IOException,
        NoHeadException, WrongRepositoryStateException, GitAPIException
    {
        CherryPickResult commandResult = cherryPick(commitList);

        @SuppressWarnings("null") @NonNull List<Ref> refList = commandResult.getCherryPickedRefs();

        // Get the start commit of the squashed region.
        RevCommit squashStartCommit = commandResult.getNewHead();
        RevWalk revWalker = new RevWalk(getRepository()); // newest first
        RevCommit root = revWalker.parseCommit(squashStartCommit);
        revWalker.sort(RevSort.COMMIT_TIME_DESC);
        revWalker.markStart(root);
        for (int i = 0; i < refList.size() - 1; i++)
            revWalker.next();
        squashStartCommit = revWalker.next();

        // Build our message of containing information of all commits that
        // were successfully cherry-picked.
        StringBuilder message = new StringBuilder();
        StringBuilder includedRevisions = new StringBuilder();
        for (int i = 0; i < commitList.size(); i++)
        {
            RevCommit current = commitList.get(i);
            // Reversing twice to allow better performance of StringBuilder with append()
            if (keepCommitMessages)
                message.append(current.getFullMessage()).append(LS);
            // Add the abbreviated commits to the message.
            if (includeCommitIds)
                includedRevisions.append(current.getId().getName()).append(",");
        }
        if (includeCommitIds)
        {
            // Remove the last ','
            includedRevisions.delete(includedRevisions.length() - 1, includedRevisions.length());
            message.append(SQUASH_PREFIX).append(includedRevisions);
        }

        String commitMessage = StringUtil.toString(message);
        @SuppressWarnings("null") @NonNull String squashStartCommitId = squashStartCommit.getId()
                                                                                         .getName();
        if (commandResult.getCherryPickedRefs().size() > 1)
        {
            try
            {
                squash(squashStartCommitId, commitMessage);
            }
            catch (Throwable e)
            {
                throw e;
            }
        }
        else
            amendCommit(commitMessage);

        // Reference the RevCommit within the CoherentCluster using RevWalk.
        RevCommit headCommit = getHeadCommit();
        // ex: "commit 0e7946e619e8c755ac579cf8556090882b27d66b 1397697076 -----p"
        return headCommit;
    }

    // Returns a list of the absolute file names.
    // The file names are the files which have been changed between the child commit and it's
    // parent.
    public String[] getFileFromRevisionsParent(RevCommit child) throws MissingObjectException,
        IncorrectObjectTypeException, IOException
    {
        Repository repository = git_.getRepository();
        RevWalk rw = new RevWalk(repository);
        @SuppressWarnings("null") @NonNull RevCommit parent = rw.parseCommit(child.getParent(0)
                                                                                  .getId());
        List<DiffEntry> diffs = getDiffEntries(parent, child);
        @SuppressWarnings("null") @NonNull String[] files = new String[diffs.size()];
        for (int i = 0; i < diffs.size(); i++)
        {
            DiffEntry diff = diffs.get(i);
            @SuppressWarnings("null") @NonNull String file = diff.getOldPath();
            // The diff's "oldPath" above returns "/dev/null" whenever any file has been added.
            // To capture the added file name, we conditionally select for the new file.
            // We use same reasoning for a renamed file.
            DiffEntry.ChangeType changeType = diff.getChangeType();
            if (changeType == DiffEntry.ChangeType.ADD || changeType == DiffEntry.ChangeType.RENAME)
            {
                @SuppressWarnings("null") @NonNull String newFile = diff.getNewPath();
                file = newFile;
            }
            files[i] = file;
        }
        return files;
    }

    /**
     * Checks out {@code branchName} as a new branch from the current branch and the current commit.
     * Equivalent to:
     * 
     * <pre>
     * # {@code commit} would be represented as the SHA1-ID in the Git terminal.
     * > git checkout -b {@code branchName} {@code commit}
     * </pre>
     */
    public Ref checkoutAsNewBranch(RevCommit commit, String branchName)
        throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException,
        CheckoutConflictException, GitAPIException
    {
        return checkoutAsNewBranch(getCommitSHA(commit), branchName);
    }

    private static String getCommitSHA(RevCommit commit)
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String result = commit.getId().getName();
        return result;
    }

    /**
     * Checks out {@code branchName} as a new branch from the current branch and the current commit.
     * Equivalent to:
     * 
     * <pre>
     * # {@code commit} would be represented as the SHA1-ID in the Git terminal.
     * > git checkout -b {@code branchName} {@code commit}
     * </pre>
     */
    public Ref checkoutAsNewBranch(String commitID, String branchName)
        throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException,
        CheckoutConflictException, GitAPIException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Ref result = git_.checkout().setCreateBranch(true)
                                                            .setName(branchName)
                                                            .setStartPoint(commitID).call();
        return result;
    }

    /**
     * Checks out {@code branchName} as a new branch from the current branch and the current commit.
     * Equivalent to:
     * 
     * <pre>
     * > git checkout -b {@code branchName}
     * </pre>
     */
    public Ref checkoutAsNewBranch(String branchName) throws RefAlreadyExistsException,
        RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Ref result = git_.checkout().setCreateBranch(true)
                                                            .setName(branchName).call();
        return result;
    }

    /**
     * Deletes the branch with {@code branchName}. Equivalent to:
     * 
     * <pre>
     * > git branch -d {@code branchName}
     * </pre>
     */
    public List<String> deleteBranch(String branchName) throws NotMergedException,
        CannotDeleteCurrentBranchException, GitAPIException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull List<@NonNull String> result = git_.branchDelete()
                                                                              .setBranchNames(branchName)
                                                                              .call();
        return result;
    }

    /**
     * Force deletes the branch with {@code branchName}. Equivalent to:
     * 
     * <pre>
     * > git branch -D {@code branchName}
     * </pre>
     * 
     * Note that the branch cannot be deleted if it is currently checked out.
     */
    public List<String> forceDeleteBranch(String branchName) throws NotMergedException,
        CannotDeleteCurrentBranchException, GitAPIException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull List<@NonNull String> result = git_.branchDelete()
                                                                              .setBranchNames(branchName)
                                                                              .setForce(true)
                                                                              .call();
        return result;
    }

    /**
     * Returns the name of the current branch. Equivalent ot:
     * 
     * <pre>
     * > git branch
     * # Selecting the branch with '*' on the left.
     * </pre>
     */
    public String getCurrentBranchName() throws IOException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String result = getRepository().getBranch();
        return result;
    }

    public List<String> getBranchNames() throws GitAPIException
    {
        List<String> result = new ArrayList<>();
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull List<@NonNull Ref> refs = git_.branchList().call();
        for (Ref ref: refs)
        {
            // Suppressed due to missing library annotations.
            @SuppressWarnings("null") @NonNull String refName = ref.getName();
            if (refName.startsWith("refs/heads/"))
                result.add(StringUtil.substring(refName, "refs/heads/".length()));
        }
        return result;
    }

    /**
     * Checks out the branch with {@code branchName}. Equivalent to:
     * 
     * <pre>
     * > git checkout {@code branchName}
     * </pre>
     */
    public Ref checkoutBranch(String branchName) throws RefAlreadyExistsException,
        RefNotFoundException, InvalidRefNameException, CheckoutConflictException, GitAPIException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Ref result = git_.checkout().setName(branchName).call();
        return result;
    }

    /**
     * Checks out the {@code revision}. Leaves the branch in detached-head state. Equivalent to:
     * 
     * <pre>
     * > git checkout {@code revision}
     * </pre>
     */
    public Ref checkout(String revision) throws RefAlreadyExistsException, RefNotFoundException,
        InvalidRefNameException, CheckoutConflictException, GitAPIException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Ref result = git_.checkout().setStartPoint(revision)
                                                            .setName(revision).call();
        return result;
    }

    /**
     * Checks out the {@code revision}. Leaves the branch in detached-head state. Equivalent to:
     * 
     * <pre>
     * > git checkout {@code revision}
     * </pre>
     */
    public Ref checkout(RevCommit revision) throws RefAlreadyExistsException, RefNotFoundException,
        InvalidRefNameException, CheckoutConflictException, GitAPIException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Ref result = git_.checkout().setStartPoint(revision)
                                                            .setName(revision.getName()).call();
        return result;
    }

    /**
     * Checks out master branch. Equivalent to:
     * 
     * <pre>
     * > git checkout master
     * </pre>
     */
    public Ref checkoutMaster() throws RefAlreadyExistsException, RefNotFoundException,
        InvalidRefNameException, CheckoutConflictException, GitAPIException
    {
        return checkoutBranch("master");
    }

    public RevCommit getHeadCommit() throws RevisionSyntaxException, AmbiguousObjectException,
        IncorrectObjectTypeException, IOException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull String headConstant = Constants.HEAD;
        return getCommit(headConstant);
    }

    public RevCommit getCommit(String shaID) throws RevisionSyntaxException,
        AmbiguousObjectException, IncorrectObjectTypeException, IOException
    {
        RevWalk walk = new RevWalk(getRepository());
        ObjectId head = getRepository().resolve(shaID);
        @SuppressWarnings("null") @NonNull RevCommit result = walk.parseCommit(head);
        return result;
    }

    /**
     * If the repository is not clean, stages all added and changed files, removes all deleted
     * files, and commits changes. Equivalent to:
     * 
     * <pre>
     * git status
     * # If repository is clean, return.
     * # Learn {@code addedOrChangedFiles} and {@code deletedFiles}.
     * > git add {@code addedOrChangedFiles}
     * > git rm {@code deletedFiles}
     * > git commit -m {@code commitMessage}
     * </pre>
     */
    public void addRemoveAndCommitAllIfNecessary(String commitMessage) throws NoWorkTreeException,
        NoFilepatternException, GitAPIException
    {
        if (!isClean())
            addRemoveAndCommitAll(commitMessage);
    }

    /**
     * Stages all added and changed files, removes all deleted files, and commits changes.
     * Equivalent to:
     * 
     * <pre>
     * git status
     * # Learn {@code addedOrChangedFiles} and {@code deletedFiles}.
     * > git add {@code addedOrChangedFiles}
     * > git rm {@code deletedFiles}
     * > git commit -m {@code commitMessage}
     * </pre>
     */
    public void addRemoveAndCommitAll(String commitMessage) throws NoFilepatternException,
        GitAPIException
    {
        git_.add().addFilepattern(".").call();
        // setAll(true) stages all changed AND deleted files.
        git_.commit().setAll(true).setMessage(commitMessage).call();
    }

    /**
     * Stages {@code files} and commits changes. Equivalent to:
     * 
     * <pre>
     * > git add {@code files}
     * > git commit -m {@code commitMessage}
     * </pre>
     */
    public void stageAndCommit(String commitMessage, File... files) throws NoFilepatternException,
        GitAPIException
    {
        for (File file: files)
        {
            // Suppressed due to missing array element annotations.
            @SuppressWarnings("null") @NonNull File safeFile = file;
            String repositoryRelativePath = getRepositoryRelativePath(safeFile);
            git_.add().addFilepattern(repositoryRelativePath).call();
        }
        git_.commit().setMessage(commitMessage).call();
    }

    /**
     * Stages {@code addedOrChangedFiles}, removes {@code deletedFiles} and commits changes.
     * Equivalent to:
     * 
     * <pre>
     * > git add {@code addedOrChangedFiles}
     * > git rm {@code deletedFiles}
     * > git commit -m {@code commitMessage}
     * </pre>
     */
    public RevCommit stageAndCommit(String commitMessage, List<File> addedOrChangedFiles,
                                    List<File> removedFiles) throws NoFilepatternException,
        GitAPIException
    {
        for (File file: addedOrChangedFiles)
        {
            String repositoryRelativePath = getRepositoryRelativePath(file);
            git_.add().addFilepattern(repositoryRelativePath).call();
        }

        for (File file: removedFiles)
        {
            String repositoryRelativePath = getRepositoryRelativePath(file);
            git_.rm().addFilepattern(repositoryRelativePath).call();
        }

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull RevCommit result = git_.commit()
                                                                  .setMessage(commitMessage).call();
        return result;
    }

    /**
     * Amends the last commit with the updated {@code commitMessage}. Equivalent to:
     * 
     * <pre>
     * > git commit --amend -m {@code commitMessage}
     * </pre>
     */
    private void amendCommit(String commitMessage) throws NoHeadException, NoMessageException,
        UnmergedPathsException, ConcurrentRefUpdateException, WrongRepositoryStateException,
        GitAPIException
    {
        git_.commit().setAmend(true).setMessage(commitMessage).call();
    }

    public String getRepositoryRelativePath(File file)
    {
        File repository = getRepositoryFolder();
        File current = file;
        String result = "";
        while (current != null && !current.getAbsolutePath().equals(repository.getAbsolutePath()))
        {
            String prefix = current.getName();
            if (current.isDirectory())
                prefix += "/";
            result = prefix + result;

            current = current.getParentFile();
        }
        return result;
    }

    /**
     * Returns the status of the repository. Equivalent to:
     * 
     * <pre>
     * > git status
     * </pre>
     */
    public Status status() throws NoWorkTreeException, GitAPIException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Status result = git_.status().call();
        return result;
    }

    /**
     * Returns <code>true</code> if the repository is clean, false otherwise. Equivalent to:
     * 
     * <pre>
     * > git status
     * # understanding whether the status returns a clean result or not.
     * </pre>
     */
    public boolean isClean() throws NoWorkTreeException, GitAPIException
    {
        // Note that I decided not to look at untracked folders for this algorithm as files are all
        // that matters and folders seem to create other issues (mainly empty commits).
        Status status = status();
        @NonNull Set<@NonNull String> removed = StatusUtil.getRemoved(status);
        int removedSize = removed.size();
        @NonNull Set<@NonNull String> missing = StatusUtil.getMissing(status);
        int missingSize = missing.size();
        @NonNull Set<@NonNull String> modified = StatusUtil.getModified(status);
        int modifiedSize = modified.size();
        @NonNull Set<@NonNull String> changed = StatusUtil.getChanged(status);
        int changedSize = changed.size();
        @NonNull Set<@NonNull String> added = StatusUtil.getAdded(status);
        int addedSize = added.size();
        @NonNull Set<@NonNull String> untrackedFiles = StatusUtil.getUntracked(status);
        int untrackedFilesSize = untrackedFiles.size();
//        Set<String> untrackedFolders = status.getUntrackedFolders();
//        int untrackedFoldersSize = untrackedFolders.size();

        if (DEBUG_CLEAN)
        {
            System.out.println("GitOperations.isClean: removed = " + removedSize + ", missing = "
                               + missingSize + ", modified = " + modifiedSize + ", changed = "
                               + changedSize + ", added = " + addedSize + ", untracked files = "
                               + untrackedFilesSize + ".");
            printSetIfNotEmpty(removed, "Removed");
            printSetIfNotEmpty(missing, "Missing");
            printSetIfNotEmpty(modified, "Modified");
            printSetIfNotEmpty(changed, "Changed");
            printSetIfNotEmpty(added, "Added");
            printSetIfNotEmpty(untrackedFiles, "Untracked files");
//            printSetIfNotEmpty(untrackedFolders, "Untracked folders");
        }

        return removedSize == 0 && missingSize == 0 //
               && modifiedSize == 0 && changedSize == 0 //
               && addedSize == 0 //
               && untrackedFilesSize == 0;
//        && untrackedFoldersSize == 0;
    }

    private static void printSetIfNotEmpty(Set<String> set, String setName)
    {
        if (set.size() != 0)
            System.out.println(setName + " = " + Debug.join(set, ", "));
    }

    /**
     * Returns the history from the latest commit to the first one. Equivalent to:
     * 
     * <pre>
     * > git log
     * </pre>
     */
    public Iterable<@NonNull RevCommit> log() throws NoHeadException, GitAPIException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Iterable<@NonNull RevCommit> result = git_.log().call();
        return result;
    }

    /**
     * Returns the history since the first ID (exclusive) until the second ID (inclusive).
     * Equivalent to:
     * 
     * <pre>
     * > git log --rev since:until
     * </pre>
     * 
     * @throws IncorrectObjectTypeException
     * @throws MissingObjectException
     */
    public Iterable<@NonNull RevCommit> log(ObjectId since, ObjectId until) throws NoHeadException,
        GitAPIException, MissingObjectException, IncorrectObjectTypeException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Iterable<@NonNull RevCommit> result = git_.log()
                                                                                     .addRange(since,
                                                                                               until)
                                                                                     .call();
        return result;
    }

    /**
     * Returns the history from the first commit to the last one. Equivalent to:
     * 
     * <pre>
     * > git log
     * # reverse the result of git log.
     * </pre>
     */
    public List<RevCommit> getCommits() throws NoHeadException, GitAPIException
    {
        List<RevCommit> commits = new ArrayList<>();
        for (RevCommit commit: log())
            commits.add(commit);
        Collections.reverse(commits);
        return commits;
    }

    /**
     * Returns the history, from start to end, both inclusive. Equivalent to:
     * 
     * <pre>
     * > git log --rev <end>:< <start> - 1> 
     * # reverse the result of git log.
     * </pre>
     * 
     * @throws IncorrectObjectTypeException
     * @throws MissingObjectException
     */
    public List<RevCommit> getCommits(RevCommit start, RevCommit end) throws NoHeadException,
        GitAPIException, MissingObjectException, IncorrectObjectTypeException
    {
        List<RevCommit> commits = new ArrayList<>();
        for (RevCommit commit: log(start, end))
            commits.add(commit);
        // include the start commit (log() excludes the start commit)
        commits.add(start);
        Collections.reverse(commits);
        return commits;
    }

    public File getGitFolder()
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull File result = getRepository().getDirectory();
        return result;
    }

    public File getRepositoryFolder()
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull File result = getGitFolder().getParentFile();
        return result;
    }

    public long getNumberOfLooseObjects() throws GitAPIException
    {
        return ((Long) getGCStatistics().get(NO_LOOSE_OBJECTS)).longValue();
    }

    // @formatter:off
    /*
     * Known GC Statistics...
     * sizeOfPackedObjects
     * sizeOfLooseObjects
     * numberOfPackedObjects
     * numberOfPackedFiles
     * numberOfPackedRefs
     * numberOfLooseRefs
     * numberOfLooseObjects
     */
    // @formatter:on
    private Properties getGCStatistics() throws GitAPIException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Properties result = git_.gc().getStatistics();
        return result;
    }

    /**
     * Garbage collects the repository. Equivalent to:
     * 
     * <pre>
     * > git gc
     * </pre>
     */
    public Properties gc() throws GitAPIException
    {
        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") @NonNull Properties result = git_.gc().call();
        return result;
    }

    /**
     * Pushes the changes to {@code remoteRepository}. Equivalent to:
     * 
     * <pre>
     * > git push -f {@code remoteRepository}
     * 
     * TODO -f is used for experimental evaluation. 
     *  It should become an argument.
     * 
     * TODO Credentials are currently hard-coded. 
     *  It should become an argument, as well.
     * </pre>
     */
    public Iterable<PushResult> push(String remoteRepository) throws InvalidRemoteException,
        TransportException, GitAPIException
    {
        CredentialsProvider credentialsProvider = new CredentialsProvider()
        {
            @Override
            public boolean supports(@SuppressWarnings("null") CredentialItem... credentialItems)
            {
                for (CredentialItem credentialItem: credentialItems)
                {
                    if (!((credentialItem instanceof Username) || (credentialItem instanceof Password)))
                        return false;
                }
                return true;
            }

            @Override
            public boolean isInteractive()
            {
                return false;
            }

            @Override
            public boolean get(@SuppressWarnings("null") URIish arg0,
                               @SuppressWarnings("null") CredentialItem... credentialItems)
                throws UnsupportedCredentialItem
            {
                for (CredentialItem credentialItem: credentialItems)
                {
                    if (credentialItem instanceof Username)
                    {
                        Username username = (Username) credentialItem;
                        username.setValue("solsticechronos");
                    }
                    else if (credentialItem instanceof Password)
                    {
                        Password password = (Password) credentialItem;
                        password.setValue("chronossolstice".toCharArray());
                    }
                    else
                        throw new UnsupportedCredentialItem(arg0, "Unsupported credential item: "
                                                                  + credentialItem);
                }
                return true;
            }
        };
        @SuppressWarnings("null") @NonNull Iterable<@NonNull PushResult> result = git_.push()
                                                                                      .setForce(true)
                                                                                      .setCredentialsProvider(credentialsProvider)
                                                                                      .setRemote(remoteRepository)
                                                                                      .call();
        return result;
    }

    public String diff(RevCommit startRevision, RevCommit endRevision) throws GitAPIException,
        RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException,
        IOException
    {
        return diff(startRevision.getId().getName(), endRevision.getId().getName());
    }

    public String diff(String startRevision, String endRevision) throws GitAPIException,
        RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException,
        IOException
    {
        ObjectId startID = getRepository().resolve(startRevision + "^{tree}");
        ObjectId endID = getRepository().resolve(endRevision + "^{tree}");

        ObjectReader reader = getRepository().newObjectReader();
        CanonicalTreeParser startTreeIter = new CanonicalTreeParser();
        startTreeIter.reset(reader, startID);
        CanonicalTreeParser endTreeIter = new CanonicalTreeParser();
        endTreeIter.reset(reader, endID);

        // Suppressed due to missing library annotations.
        @SuppressWarnings("null") List<DiffEntry> diffs = git_.diff().setOldTree(startTreeIter)
                                                              .setNewTree(endTreeIter).call();

        String result;
        try (ByteArrayOutputStream os = new ByteArrayOutputStream())
        {
            DiffFormatter df = getDiffFormatter(os);
            for (DiffEntry diff: diffs)
                df.format(diff);
            result = StringUtil.toString(os);
        }
        return result;
    }
}

