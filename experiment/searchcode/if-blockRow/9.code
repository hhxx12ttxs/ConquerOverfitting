JobConf job = new JobConf(ConfigurationManager.getCachedJobConf());
Path path = new Path( fname );

//if the file already exists on HDFS, remove it.
MapReduceTool.deleteFileIfExistOnHDFS( fname );

//core write
writeBinaryBlockFrameToHDFS(path, job, src, rlen, clen);

