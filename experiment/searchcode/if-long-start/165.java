/*
 * Copyright 2011 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.drools.guvnor.server;

import com.google.gwt.user.client.rpc.SerializationException;
import org.apache.commons.lang.StringEscapeUtils;
import org.drools.guvnor.client.common.AssetFormats;
import org.drools.guvnor.client.rpc.*;
import org.drools.guvnor.server.builder.AssetItemValidator;
import org.drools.guvnor.server.builder.BRMSPackageBuilder;
import org.drools.guvnor.server.builder.DSLLoader;
import org.drools.guvnor.server.builder.PageResponseBuilder;
import org.drools.guvnor.server.builder.pagerow.ArchivedAssetPageRowBuilder;
import org.drools.guvnor.server.builder.pagerow.AssetPageRowBuilder;
import org.drools.guvnor.server.builder.pagerow.QuickFindPageRowBuilder;
import org.drools.guvnor.server.cache.RuleBaseCache;
import org.drools.guvnor.server.contenthandler.ContentHandler;
import org.drools.guvnor.server.contenthandler.ContentManager;
import org.drools.guvnor.server.contenthandler.ICanRenderSource;
import org.drools.guvnor.server.contenthandler.IRuleAsset;
import org.drools.guvnor.server.repository.MailboxService;
import org.drools.guvnor.server.security.RoleType;
import org.drools.guvnor.server.util.*;
import org.drools.repository.*;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;

import java.text.DateFormat;
import java.util.*;

/**
 * Handles operations for Assets
 */
@Name("org.drools.guvnor.server.RepositoryAssetOperations")
@AutoCreate
public class RepositoryAssetOperations {

    private RulesRepository repository;

    private static final LoggingHelper log = LoggingHelper.getLogger(RepositoryAssetOperations.class);

    private final String[] registeredFormats;

    public RepositoryAssetOperations() {
        //Load recognised formats from configuration file
        AssetEditorConfigurationParser parser = new AssetEditorConfigurationParser();
        List<AssetEditorConfiguration> rfs = parser.getAssetEditors();
        this.registeredFormats = new String[rfs.size()];
        for (int i = 0; i < rfs.size(); i++) {
            AssetEditorConfiguration config = rfs.get(i);
            registeredFormats[i] = config.getFormat();
        }
    }

    public void setRulesRepository(RulesRepository repository) {
        this.repository = repository;
    }

    public RulesRepository getRulesRepository() {
        return repository;
    }

    public String renameAsset(String uuid,
                              String newName) {
        return getRulesRepository().renameAsset(uuid,
                newName);
    }

    protected BuilderResult validateAsset(RuleAsset asset) {
        try {
            ContentHandler handler = ContentManager
                    .getHandler(asset.getFormat());
            AssetItem item = getRulesRepository().loadAssetByUUID(asset.uuid);

            handler.storeAssetContent(asset,
                    item);

            AssetItemValidator assetItemValidator = new AssetItemValidator(handler,
                    item);
            return assetItemValidator.validate();

        } catch (Exception e) {
            log.error("Unable to build asset.",
                    e);
            BuilderResult result = new BuilderResult();
            result.addLine(createBuilderResultLine(asset));
            return result;
        }
    }

    private BuilderResultLine createBuilderResultLine(RuleAsset asset) {
        BuilderResultLine builderResultLine = new BuilderResultLine();
        builderResultLine.setAssetName(asset.name);
        builderResultLine.setAssetFormat(asset.getFormat());
        builderResultLine.setMessage("Unable to validate this asset. (Check log for detailed messages).");
        builderResultLine.setUuid(asset.uuid);
        return builderResultLine;
    }

    public String checkinVersion(RuleAsset asset) throws SerializationException {
        AssetItem repoAsset = getRulesRepository().loadAssetByUUID(asset.getUuid());
        if (isAssetUpdatedInRepository(asset,
                repoAsset)) {
            return "ERR: Unable to save this asset, as it has been recently updated by [" + repoAsset.getLastContributor() + "]";
        }

        MetaData meta = asset.getMetaData();
        MetaDataMapper.getInstance().copyFromMetaData(meta,
                repoAsset);

        repoAsset.updateDateEffective(dateToCalendar(meta.getDateEffective()));
        repoAsset.updateDateExpired(dateToCalendar(meta.getDateExpired()));

        repoAsset.updateCategoryList(meta.getCategories());
        repoAsset.updateDescription(asset.getDescription());

        ContentHandler handler = ContentManager.getHandler(repoAsset.getFormat());
        handler.storeAssetContent(asset,
                repoAsset);

        if (!asset.getFormat().equals(AssetFormats.TEST_SCENARIO) || asset.getFormat().equals(AssetFormats.ENUMERATION)) {
            PackageItem pkg = repoAsset.getPackage();
            pkg.updateBinaryUpToDate(false);
            RuleBaseCache.getInstance().remove(pkg.getUUID());
        }
        repoAsset.checkin(asset.getCheckinComment());

        return repoAsset.getUUID();
    }

    private Calendar dateToCalendar(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    private boolean isAssetUpdatedInRepository(RuleAsset asset,
                                               AssetItem repoAsset) {
        return asset.getLastModified().before(repoAsset.getLastModified().getTime());
    }

    public void restoreVersion(String versionUUID,
                               String assetUUID,
                               String comment) {
        AssetItem old = getRulesRepository().loadAssetByUUID(versionUUID);
        AssetItem head = getRulesRepository().loadAssetByUUID(assetUUID);

        log.info("USER:" + getCurrentUserName() + " RESTORE of asset: [" + head.getName() + "] UUID: [" + head.getUUID() + "] with historical version number: [" + old.getVersionNumber());

        getRulesRepository().restoreHistoricalAsset(old,
                head,
                comment);
    }

    protected TableDataResult loadItemHistory(final VersionableItem item) {
        Iterator<VersionableItem> it = item.getHistory();
        //AssetHistoryIterator it = assetItem.getHistory();

        // MN Note: this uses the lazy iterator, but then loads the whole lot
        // up, and returns it.
        // The reason for this is that the GUI needs to show things in numeric
        // order by the version number.
        // When a version is restored, its previous version is NOT what you
        // thought it was - due to how JCR works
        // (its more like CVS then SVN). So to get a linear progression of
        // versions, we use the incrementing version number,
        // and load it all up and sort it. This is not ideal.
        // In future, we may do a "restore" instead just by copying content into
        // a new version, not restoring a node,
        // in which case the iterator will be in order (or you can just walk all
        // the way back).
        // So if there are performance problems with looking at lots of
        // historical versions, look at this nasty bit of code.
        List<TableDataRow> result = new ArrayList<TableDataRow>();
        while (it.hasNext()) {
            VersionableItem historical = (VersionableItem) it.next();
            long versionNumber = historical.getVersionNumber();
            if (isHistory(item,
                    versionNumber)) {
                result.add(createHistoricalRow(
                        historical));
            }
        }

        TableDataResult table = new TableDataResult();
        table.data = result.toArray(new TableDataRow[result.size()]);

        return table;
    }

    private boolean isHistory(VersionableItem item,
                              long versionNumber) {
        //return versionNumber != 0 && versionNumber != item.getVersionNumber();
        //we do return the LATEST version as part of the history. 
        return versionNumber != 0;
    }

    private TableDataRow createHistoricalRow(VersionableItem historical) {
        final DateFormat dateFormatter = DateFormat.getInstance();
        TableDataRow tableDataRow = new TableDataRow();
        tableDataRow.id = historical.getVersionSnapshotUUID();
        tableDataRow.values = new String[4];
        tableDataRow.values[0] = Long.toString(historical.getVersionNumber());
        tableDataRow.values[1] = historical.getCheckinComment();
        tableDataRow.values[2] = dateFormatter.format(historical
                .getLastModified().getTime());
        tableDataRow.values[3] = historical.getStateDescription();
        return tableDataRow;
    }

    /**
     * @param skip
     * @param numRows
     * @return
     * @throws SerializationException
     * @deprecated in favour of {@link loadArchivedAssets(PageRequest)}
     */
    protected TableDataResult loadArchivedAssets(int skip,
                                                 int numRows) {
        List<TableDataRow> result = new ArrayList<TableDataRow>();
        RepositoryFilter filter = new AssetItemFilter();

        AssetItemIterator it = getRulesRepository().findArchivedAssets();
        it.skip(skip);
        int count = 0;
        while (it.hasNext()) {

            AssetItem archived = (AssetItem) it.next();

            if (filter.accept(archived,
                    "read")) {
                result.add(createArchivedRow(archived));
                count++;
            }
            if (count == numRows) {
                break;
            }
        }

        return createArchivedTable(result,
                it);
    }

    private TableDataRow createArchivedRow(AssetItem archived) {
        TableDataRow row = new TableDataRow();
        row.id = archived.getUUID();
        row.values = new String[5];
        row.values[0] = archived.getName();
        row.values[1] = archived.getFormat();
        row.values[2] = archived.getPackageName();
        row.values[3] = archived.getLastContributor();
        row.values[4] = Long.toString(archived.getLastModified().getTime()
                .getTime());
        return row;
    }

    private TableDataResult createArchivedTable(List<TableDataRow> result,
                                                AssetItemIterator it) {
        TableDataResult table = new TableDataResult();
        table.data = result.toArray(new TableDataRow[result.size()]);
        table.currentPosition = it.getPosition();
        table.total = it.getSize();
        table.hasNext = it.hasNext();
        return table;
    }

    protected PageResponse<AdminArchivedPageRow> loadArchivedAssets(PageRequest request) {
        // Do query
        long start = System.currentTimeMillis();
        AssetItemIterator iterator = getRulesRepository().findArchivedAssets();
        log.debug("Search time: " + (System.currentTimeMillis() - start));

        // Populate response
        long totalRowsCount = iterator.getSize();

        List<AdminArchivedPageRow> rowList = new ArchivedAssetPageRowBuilder()
                .withPageRequest(request)
                .withContent(iterator)
                .build();

        PageResponse<AdminArchivedPageRow> response = new PageResponseBuilder<AdminArchivedPageRow>()
                .withStartRowIndex(request.getStartRowIndex())
                .withPageRowList(rowList)
                .withLastPage(!iterator.hasNext())
                .buildWithTotalRowCount(totalRowsCount);

        long methodDuration = System.currentTimeMillis() - start;
        log.debug("Searched for Archived Assests in " + methodDuration + " ms.");
        return response;
    }

    /**
     * @param packageUuid
     * @param formats
     * @param skip
     * @param numRows
     * @param tableConfig
     * @return
     * @throws SerializationException
     * @deprecated in favour of {@link findAssetPage(AssetPageRequest)}
     */
    protected TableDataResult listAssets(String packageUuid,
                                         String formats[],
                                         int skip,
                                         int numRows,
                                         String tableConfig) {
        long start = System.currentTimeMillis();
        PackageItem pkg = getRulesRepository().loadPackageByUUID(packageUuid);
        AssetItemIterator it;
        if (formats.length > 0) {
            it = pkg.listAssetsByFormat(formats);
        } else {
            it = pkg.listAssetsNotOfFormat(AssetFormatHelper
                    .listRegisteredTypes());
        }
        TableDisplayHandler handler = new TableDisplayHandler(tableConfig);
        log.debug("time for asset list load: "
                + (System.currentTimeMillis() - start));
        return handler.loadRuleListTable(it,
                skip,
                numRows);
    }

    /**
     * @param searchText
     * @param searchArchived
     * @param skip
     * @param numRows
     * @return
     * @throws SerializationException
     * @deprecated in favour of {@link quickFindAsset(QueryPageRequest)}
     */
    protected TableDataResult quickFindAsset(String searchText,
                                             boolean searchArchived,
                                             int skip,
                                             int numRows)
            throws SerializationException {
        String search = searchText.replace('*',
                '%');

        if (!search.endsWith("%")) {
            search += "%";
        }

        List<AssetItem> resultList = new ArrayList<AssetItem>();

        long start = System.currentTimeMillis();
        AssetItemIterator it = getRulesRepository().findAssetsByName(search,
                searchArchived);
        log.debug("Search time: " + (System.currentTimeMillis() - start));

        RepositoryFilter filter = new AssetItemFilter();

        while (it.hasNext()) {
            AssetItem ai = it.next();
            if (filter.accept(ai,
                    RoleType.PACKAGE_READONLY.getName())) {
                resultList.add(ai);
            }
        }

        TableDisplayHandler handler = new TableDisplayHandler("searchresults");
        return handler.loadRuleListTable(resultList,
                skip,
                numRows);
    }

    /**
     * @param text
     * @param seekArchived
     * @param skip
     * @param numRows
     * @return
     * @throws SerializationException
     * @deprecated in favour of {@link queryFullText(QueryPageRequest)}
     */
    protected TableDataResult queryFullText(String text,
                                            boolean seekArchived,
                                            int skip,
                                            int numRows) throws SerializationException {

        List<AssetItem> resultList = new ArrayList<AssetItem>();
        RepositoryFilter filter = new PackageFilter();

        AssetItemIterator assetItemIterator = getRulesRepository().queryFullText(text,
                seekArchived);
        while (assetItemIterator.hasNext()) {
            AssetItem assetItem = assetItemIterator.next();
            PackageConfigData data = new PackageConfigData();
            data.setUuid(assetItem.getPackage().getUUID());
            if (filter.accept(data,
                    RoleType.PACKAGE_READONLY.getName())) {
                resultList.add(assetItem);
            }
        }

        TableDisplayHandler handler = new TableDisplayHandler("searchresults");
        return handler.loadRuleListTable(resultList,
                skip,
                numRows);
    }

    // TODO: Very hard to unit test -> needs refactoring
    protected String buildAssetSource(RuleAsset asset) throws SerializationException {
        ContentHandler handler = ContentManager.getHandler(asset.getFormat());

        StringBuilder stringBuilder = new StringBuilder();
        if (handler.isRuleAsset()) {
            BRMSPackageBuilder builder = new BRMSPackageBuilder();
            // now we load up the DSL files
            PackageItem packageItem = getRulesRepository().loadPackage(asset.getMetaData().getPackageName());
            builder.setDSLFiles(DSLLoader.loadDSLMappingFiles(packageItem));
            if (asset.getMetaData().isBinary()) {
                AssetItem item = getRulesRepository().loadAssetByUUID(
                        asset.getUuid());

                handler.storeAssetContent(asset,
                        item);
                ((IRuleAsset) handler).assembleDRL(builder,
                        item,
                        stringBuilder);
            } else {
                ((IRuleAsset) handler).assembleDRL(builder,
                        asset,
                        stringBuilder);
            }
        } else {
            if (handler instanceof ICanRenderSource) {
                ICanRenderSource crs = (ICanRenderSource) handler;
                crs.assembleSource(asset.getContent(),
                        stringBuilder);
            }
        }
        return stringBuilder.toString();
    }

    protected PageResponse<AssetPageRow> findAssetPage(AssetPageRequest request) {
        log.debug("Finding asset page of packageUuid ("
                + request.getPackageUuid() + ")");
        long start = System.currentTimeMillis();

        PackageItem packageItem = getRulesRepository().loadPackageByUUID(request.getPackageUuid());

        AssetItemIterator iterator;
        if (request.getFormatInList() != null) {
            if (request.getFormatIsRegistered() != null) {
                throw new IllegalArgumentException("Combining formatInList and formatIsRegistered is not yet supported.");
            }
            iterator = packageItem.listAssetsByFormat(request.getFormatInList());

        } else {
            if (request.getFormatIsRegistered() != null && request.getFormatIsRegistered().equals(Boolean.FALSE)) {
                iterator = packageItem.listAssetsNotOfFormat(registeredFormats);
            } else {
                iterator = packageItem.queryAssets("");
            }
        }

        // Populate response
        long totalRowsCount = iterator.getSize();

        List<AssetPageRow> rowList = new AssetPageRowBuilder()
                .withPageRequest(request)
                .withContent(iterator)
                .build();

        PageResponse<AssetPageRow> response = new PageResponseBuilder<AssetPageRow>()
                .withStartRowIndex(request.getStartRowIndex())
                .withPageRowList(rowList)
                .withLastPage(!iterator.hasNext())
                .buildWithTotalRowCount(totalRowsCount);
        long methodDuration = System.currentTimeMillis() - start;
        log.debug("Found asset page of packageUuid ("
                + request.getPackageUuid() + ") in " + methodDuration + " ms.");
        return response;
    }

    protected PageResponse<QueryPageRow> quickFindAsset(QueryPageRequest request) {
        // Setup parameters
        String search = request.getSearchText().replace('*',
                '%');
        if (!search.startsWith("%")) {
            search = "%" + search;
        }
        if (!search.endsWith("%")) {
            search += "%";
        }

        // Do query
        long start = System.currentTimeMillis();
        AssetItemIterator iterator = getRulesRepository().findAssetsByName(search,
                request.isSearchArchived(),
                request.isCaseSensitive());
        log.debug("Search time: " + (System.currentTimeMillis() - start));

        // Populate response
        long totalRowsCount = iterator.getSize();

        List<QueryPageRow> rowList = new QuickFindPageRowBuilder()
                .withPageRequest(request)
                .withContent(iterator)
                .build();

        PageResponse<QueryPageRow> response = new PageResponseBuilder<QueryPageRow>()
                .withStartRowIndex(request.getStartRowIndex())
                .withPageRowList(rowList)
                .withLastPage(!iterator.hasNext())
                .buildWithTotalRowCount(totalRowsCount);

        long methodDuration = System.currentTimeMillis() - start;
        log.debug("Queried repository (Quick Find) for (" + search + ") in " + methodDuration + " ms.");
        return response;
    }

    protected void lockAsset(String uuid) {
        AssetLockManager lockManager = AssetLockManager.instance();

        String userName;
        if (Contexts.isApplicationContextActive()) {
            userName = Identity.instance().getCredentials().getUsername();
        } else {
            userName = "anonymous";
        }

        log.info("Locking asset uuid=" + uuid + " for user [" + userName + "]");

        lockManager.lockAsset(uuid,
                userName);
    }

    protected void unLockAsset(String uuid) {
        AssetLockManager alm = AssetLockManager.instance();
        log.info("Unlocking asset [" + uuid + "]");
        alm.unLockAsset(uuid);
    }

    protected String getAssetLockerUserName(String uuid) {
        AssetLockManager alm = AssetLockManager.instance();

        String userName = alm.getAssetLockerUserName(uuid);

        log.info("Asset locked by [" + userName + "]");

        return userName;
    }

    protected RuleAsset loadAsset(AssetItem item) throws SerializationException {

        RuleAsset asset = new RuleAsset();
        asset.setUuid(item.getUUID());
        asset.setName(item.getName());
        asset.setDescription(item.getDescription());
        asset.setLastModified(item.getLastModified().getTime());
        asset.setLastContributor(item.getLastContributor());
        asset.setState((item.getState() != null) ? item.getState().getName() : "");
        asset.setDateCreated(item.getCreatedDate().getTime());
        asset.setCheckinComment(item.getCheckinComment());
        asset.setVersionNumber(item.getVersionNumber());
        asset.setFormat(item.getFormat());

        asset.setMetaData(populateMetaData(item));
        ContentHandler handler = ContentManager.getHandler(asset.getFormat());
        handler.retrieveAssetContent(asset,
                item);

        return asset;
    }

    /**
     * Populate meta data with asset specific info.
     */
    MetaData populateMetaData(AssetItem item) {
        MetaData meta = populateMetaData((VersionableItem) item);

        meta.setPackageName(item.getPackageName());
        meta.setPackageUUID(item.getPackage().getUUID());
        meta.setBinary(item.isBinary());

        List<CategoryItem> categories = item.getCategories();
        fillMetaCategories(meta,
                categories);
        meta.setDateEffective(calendarToDate(item.getDateEffective()));
        meta.setDateExpired(calendarToDate(item.getDateExpired()));
        return meta;

    }

    /**
     * read in the meta data, populating all dublin core and versioning stuff.
     */
    MetaData populateMetaData(VersionableItem item) {
        MetaData meta = new MetaData();
        MetaDataMapper.getInstance().copyToMetaData(meta,
                item);

        //problematic implementation of getPrecedingVersion and getPrecedingVersion().
        //commented out temporarily as this is used by the front end. 
        //meta.hasPreceedingVersion = item.getPrecedingVersion() != null;
        //meta.hasSucceedingVersion = item.getPrecedingVersion() != null;
        return meta;
    }

    private void fillMetaCategories(MetaData meta,
                                    List<CategoryItem> categories) {
        meta.setCategories(new String[categories.size()]);
        for (int i = 0; i < meta.getCategories().length; i++) {
            CategoryItem cat = (CategoryItem) categories.get(i);
            meta.getCategories()[i] = cat.getFullPath();
        }
    }

    private Date calendarToDate(Calendar createdDate) {
        if (createdDate == null) {
            return null;
        }
        return createdDate.getTime();
    }

    protected void clearAllDiscussionsForAsset(final String assetId) {
        RulesRepository repo = getRulesRepository();
        AssetItem asset = repo.loadAssetByUUID(assetId);
        asset.updateStringProperty("",
                "discussion");
        repo.save();

        push("discussion",
                assetId);
    }

    protected List<DiscussionRecord> addToDiscussionForAsset(String assetId,
                                                             String comment) {
        RulesRepository repository = getRulesRepository();
        AssetItem asset = repository.loadAssetByUUID(assetId);
        Discussion dp = new Discussion();
        List<DiscussionRecord> discussion = dp.fromString(asset.getStringProperty(Discussion.DISCUSSION_PROPERTY_KEY));
        discussion.add(new DiscussionRecord(repository.getSession().getUserID(),
                StringEscapeUtils.escapeXml(comment)));
        asset.updateStringProperty(dp.toString(discussion),
                Discussion.DISCUSSION_PROPERTY_KEY,
                false);
        repository.save();

        push("discussion",
                assetId);

        MailboxService.getInstance().recordItemUpdated(asset);

        return discussion;
    }

    private void push(String messageType,
                      String message) {
        Backchannel.getInstance().publish(new PushResponse(messageType,
                message));
    }

    private String getCurrentUserName() {
        return getRulesRepository().getSession().getUserID();
    }
}

