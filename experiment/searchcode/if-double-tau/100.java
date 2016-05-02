package vagueobjects.ir.lda.online.demo;
/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import org.apache.commons.io.IOUtils;
import vagueobjects.ir.lda.tokens.Documents;
import vagueobjects.ir.lda.online.OnlineLDA;
import vagueobjects.ir.lda.online.Result;
import vagueobjects.ir.lda.tokens.PlainVocabulary;
import vagueobjects.ir.lda.tokens.Vocabulary;

import java.io.*;
import java.util.*;
import java.util.List;

public class Execution {
 
    public static void main(String[] args) throws Exception{ 
        if(args.length<0){
            System.out.println("please provide paths for document directory and dictionary file");
            return;
        }
        String docPath= args[0];
        String dictPath = args[1];
  

        int D=  10800;
        int K = 100;
        int batchSize= 1024;

        double tau =  1d;
        double kappa =  0.8d;

        double alpha = 1.d/K;
        double eta = 1.d/K;

        Vocabulary vocabulary = new PlainVocabulary( dictPath);
        OnlineLDA lda = new OnlineLDA(vocabulary.size(),K, D, alpha, eta, tau, kappa);
        List<String> docs = readDocs(docPath);
        long delta=0;

        for(int i=0; i*batchSize < docs.size();++i){
            int max = Math.min((i+1)*batchSize, docs.size());
            Documents documents = new Documents(docs.subList(i * batchSize, max), vocabulary);
            Result result = lda.workOn(documents);
            System.out.println(result);
        }
        System.out.println("Time " + delta/1000);
    }
 
    static List<String> readDocs(String path) throws IOException{

        List<String> strings = new ArrayList<String>();
        File dir = new File(path);
        for(File f :  dir.listFiles()){
            InputStream is = new FileInputStream(f);
            StringWriter writer = new StringWriter();
            IOUtils.copy(is, writer);
            strings.add(writer.toString());
            is.close();

        }
        return strings;
    }

}

