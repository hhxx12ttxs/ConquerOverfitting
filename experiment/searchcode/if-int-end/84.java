package com.sfeir.wolfengine.server.rpc.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs.FileObject;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sfeir.wolfengine.client.exception.WolfEngineException;
import com.sfeir.wolfengine.client.model.entity.SearchResult;
import com.sfeir.wolfengine.client.model.entity.datamanagement.GwtFile;
import com.sfeir.wolfengine.client.rpc.FileRpcService;
import com.sfeir.wolfengine.server.entity.datamanagement.Author;
import com.sfeir.wolfengine.server.security.AuthorizationChecker;
import com.sfeir.wolfengine.server.service.FileService;

@Singleton
public class FileRpcServiceImpl extends RemoteServiceServlet implements FileRpcService {

    private static final long serialVersionUID = -8721246815989948989L;

    @Inject
    private FileService fileService;
    @Inject private AuthorizationChecker authorizationChecker;

    @Override
    public SearchResult<GwtFile> list(int limit, int page) throws WolfEngineException {
        Author user = authorizationChecker.getAuthor();
        List<FileObject> listeFiles = fileService.list("/", user);
        List<GwtFile> liste = new ArrayList<GwtFile>(limit);
        int first = (page) * limit;
        int end = first + limit;
        int length = listeFiles.size();
        if (end > length)
            end = length;
        for (int i = first; i < end; i++) {
            FileObject file = listeFiles.get(i);
            GwtFile gf = new GwtFile();
            gf.setName(file.getName().getBaseName());
            gf.setPath(fileService.getRelativePath(file));
            gf.setType(fileService.getTypeFile(file));
            gf.setExtension(file.getName().getExtension());
            liste.add(gf);
        }
        return new SearchResult<GwtFile>(liste, length, limit, page);
    }

    @Override
    public Boolean delete(String fileName) throws WolfEngineException {
        Author user = authorizationChecker.getAuthor();
        return fileService.deleteFile(fileName, user);
    }

}

