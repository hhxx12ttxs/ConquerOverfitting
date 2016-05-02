package SevenZip;
import java.io.IOException;
import java.io.InputStream;



// import SevenZip.Archive.Common.CoderMixer2MT;


class SevenZip_Decoder {
    
    boolean _bindInfoExPrevIsDefined;
    BindInfoEx _bindInfoExPrev;
    
    boolean _multiThread;
    
    // CoderMixer2MT _mixerCoderMTSpec;
    CoderMixer2ST _mixerCoderSTSpec;
    CoderMixer2 _mixerCoderCommon;
    
    ICompressCoder2 _mixerCoder;
    ObjectVector<Object> _decoders;
    
    public SevenZip_Decoder(boolean multiThread) {
        _multiThread = multiThread;
        _bindInfoExPrevIsDefined = false;
        _bindInfoExPrev = new BindInfoEx();
        
        _mixerCoder = null;
        _decoders = new ObjectVector<Object>();
        
        // #ifndef EXCLUDE_COM -- LoadMethodMap();
    }
    
    static void ConvertFolderItemInfoToBindInfo(Folder folder,BindInfoEx bindInfo) {
        bindInfo.Clear();
        
        for (int i = 0; i < folder.BindPairs.size(); i++) {
            BindPair bindPair = new BindPair();
            bindPair.InIndex = folder.BindPairs.get(i).InIndex;
            bindPair.OutIndex = folder.BindPairs.get(i).OutIndex;
            bindInfo.BindPairs.add(bindPair);
        }
        int outStreamIndex = 0;
        for (int i = 0; i < folder.Coders.size(); i++) {
            CoderStreamsInfo coderStreamsInfo = new CoderStreamsInfo();
            SevenZip_CoderInfo coderInfo = folder.Coders.get(i);
            coderStreamsInfo.NumInStreams = coderInfo.NumInStreams;
            coderStreamsInfo.NumOutStreams = coderInfo.NumOutStreams;
            bindInfo.Coders.add(coderStreamsInfo);
            AltCoderInfo altCoderInfo = coderInfo.AltCoders.Front();
            bindInfo.CoderMethodIDs.add(altCoderInfo.MethodID);
            for (int j = 0; j < coderStreamsInfo.NumOutStreams; j++, outStreamIndex++)
                if (folder.FindBindPairForOutStream(outStreamIndex) < 0)
                    bindInfo.OutStreams.add(outStreamIndex);
        }
        for (int i = 0; i < folder.PackStreams.size(); i++)
            bindInfo.InStreams.add(folder.PackStreams.get(i));
    }
    static boolean AreCodersEqual(CoderStreamsInfo a1, CoderStreamsInfo a2) {
        return (a1.NumInStreams == a2.NumInStreams) &&
                (a1.NumOutStreams == a2.NumOutStreams);
    }
    
    static boolean AreBindPairsEqual(BindPair a1, BindPair a2) {
        return (a1.InIndex == a2.InIndex) &&
                (a1.OutIndex == a2.OutIndex);
    }
    
    static boolean AreBindInfoExEqual(BindInfoEx a1, BindInfoEx a2) {
        if (a1.Coders.size() != a2.Coders.size())
            return false;
        int i;
        for (i = 0; i < a1.Coders.size(); i++)
            if (!AreCodersEqual(a1.Coders.get(i), a2.Coders.get(i)))
                return false;
        if (a1.BindPairs.size() != a2.BindPairs.size())
            return false;
        for (i = 0; i < a1.BindPairs.size(); i++)
            if (!AreBindPairsEqual(a1.BindPairs.get(i), a2.BindPairs.get(i)))
                return false;
        for (i = 0; i < a1.CoderMethodIDs.size(); i++)
            if (a1.CoderMethodIDs.get(i) != a2.CoderMethodIDs.get(i))
                return false;
        if (a1.InStreams.size() != a2.InStreams.size())
            return false;
        if (a1.OutStreams.size() != a2.OutStreams.size())
            return false;
        return true;
    }
    
    int Decode(SevenZip.IInStream inStream,
            long startPos,
            LongVector packSizes, int packSizesOffset, // const UInt64 *packSizes,
            Folder folderInfo,
            java.io.OutputStream outStream,
            SevenZip.ICompressProgressInfo compressProgress // , // ICompressProgressInfo *compressProgress,
            // _ST_MODE boolean mtMode, int numThreads
            ) throws IOException {
        
        
        ObjectVector<java.io.InputStream> inStreams = new ObjectVector<java.io.InputStream>(); // CObjectVector< CMyComPtr<ISequentialInStream> >
        
        LockedInStream lockedInStream = new LockedInStream();
        lockedInStream.Init(inStream);
        
        for (int j = 0; j < folderInfo.PackStreams.size(); j++) {
            LockedSequentialInStreamImp lockedStreamImpSpec = new LockedSequentialInStreamImp();
            java.io.InputStream lockedStreamImp = lockedStreamImpSpec;
            lockedStreamImpSpec.Init(lockedInStream, startPos);
            startPos += packSizes.get(j+packSizesOffset);
            
            LimitedSequentialInStream streamSpec = new LimitedSequentialInStream();
            java.io.InputStream inStream2 = streamSpec;
            streamSpec.SetStream(lockedStreamImp);
            streamSpec.Init(packSizes.get(j+packSizesOffset));
            inStreams.add(inStream2);
        }
        
        int numCoders = folderInfo.Coders.size();
        
        BindInfoEx bindInfo = new BindInfoEx();
        ConvertFolderItemInfoToBindInfo(folderInfo, bindInfo);
        boolean createNewCoders;
        if (!_bindInfoExPrevIsDefined)
            createNewCoders = true;
        else
            createNewCoders = !AreBindInfoExEqual(bindInfo, _bindInfoExPrev);
        
        
        
        if (createNewCoders) {
            int i;
            _decoders.clear();
            
            if (_mixerCoder != null) _mixerCoder.close(); // _mixerCoder.Release();
            
            if (_multiThread) {
                /*
                _mixerCoderMTSpec = new CoderMixer2MT();
                _mixerCoder = _mixerCoderMTSpec;
                _mixerCoderCommon = _mixerCoderMTSpec;
                */
                throw new IOException("multithreaded decoder not implemented");
            } else {
                _mixerCoderSTSpec = new CoderMixer2ST();
                _mixerCoder = _mixerCoderSTSpec;
                _mixerCoderCommon = _mixerCoderSTSpec;
            }
            _mixerCoderCommon.SetBindInfo(bindInfo);
            
            for (i = 0; i < numCoders; i++) {
                SevenZip_CoderInfo coderInfo = folderInfo.Coders.get(i);
                AltCoderInfo altCoderInfo = coderInfo.AltCoders.Front();
                /*
                #ifndef EXCLUDE_COM
                        CMethodInfo methodInfo;
                if (!GetMethodInfo(altCoderInfo.MethodID, methodInfo))
                    return E_NOTIMPL;
                #endif
                 */
                
                if (coderInfo.IsSimpleCoder()) {
                    ICompressCoder decoder = null;
                    ICompressFilter filter = null;
                    
                    // #ifdef COMPRESS_LZMA
                    if (altCoderInfo.MethodID.equals(MethodID.k_LZMA))
                        decoder = new SevenZip.LZMADecoder(); // NCompress::NLZMA::CDecoder;
                    
                    if (altCoderInfo.MethodID.equals(MethodID.k_PPMD))
                        System.out.println("PPMD not implemented"); // decoder = new NCompress::NPPMD::CDecoder;
                    
                    if (altCoderInfo.MethodID.equals(MethodID.k_BCJ_X86))
                        filter = new SevenZip.BCJ_x86_Decoder();
                    
                    if (altCoderInfo.MethodID.equals(MethodID.k_Deflate))
                        System.out.println("DEFLATE not implemented"); // decoder = new NCompress::NDeflate::NDecoder::CCOMCoder;
                    
                    if (altCoderInfo.MethodID.equals(MethodID.k_BZip2))
                        System.out.println("BZIP2 not implemented"); // decoder = new NCompress::NBZip2::CDecoder;
                    
                    if (altCoderInfo.MethodID.equals(MethodID.k_Copy))
                        decoder = new SevenZip.Decoder(); // decoder = new NCompress::CCopyCoder;
                    
                    if (altCoderInfo.MethodID.equals(MethodID.k_7zAES))
                        throw new IOException("k_7zAES not implemented"); // filter = new NCrypto::NSevenZ::CDecoder;
                    
                    if (filter != null) {
                        FilterCoder coderSpec = new FilterCoder();
                        decoder = coderSpec;
                        coderSpec.Filter = filter;
                    }
                    /*
                        #ifndef EXCLUDE_COM
                                if (decoder == 0) {
                            RINOK(_libraries.CreateCoderSpec(methodInfo.FilePath,
                                    methodInfo.Decoder, &decoder));
                                }
                        #endif
                     */
                    if (decoder == null)
                        return HRESULT.E_NOTIMPL;
                    
                    _decoders.add(decoder);
                    
                    if (_multiThread) {
                        // _mixerCoderMTSpec.AddCoder(decoder);
                        throw new IOException("Multithreaded decoder is not implemented");
                    } else {
                        _mixerCoderSTSpec.AddCoder(decoder, false);
                    }
                } else {

                    ICompressCoder2 decoder = null;
                     
                    if (altCoderInfo.MethodID.equals(MethodID.k_BCJ2)) {
                        decoder = new SevenZip.BCJ2_x86_Decoder();
                    }
                            
                     
                            if (decoder == null)
                                return HRESULT.E_NOTIMPL;
                     
                    _decoders.add(decoder);
                    if (_multiThread) {
                       // _mixerCoderMTSpec.AddCoder2(decoder);
                       throw new IOException("Multithreaded decoder is not implemented");
                    } else {
                        _mixerCoderSTSpec.AddCoder2(decoder, false);
                    }
                }
            }
            _bindInfoExPrev = bindInfo;
            _bindInfoExPrevIsDefined = true;
        }
        
        int i;
        _mixerCoderCommon.ReInit();
        
        int packStreamIndex = 0, unPackStreamIndex = 0;
        int coderIndex = 0;
        
        for (i = 0; i < numCoders; i++) {
            SevenZip_CoderInfo coderInfo = folderInfo.Coders.get(i);
            AltCoderInfo altCoderInfo = coderInfo.AltCoders.Front();
            Object decoder = _decoders.get(coderIndex); // CMyComPtr<IUnknown> &decoder = _decoders[coderIndex];
            
            {
                try {
                    ICompressSetDecoderProperties2 setDecoderProperties = (ICompressSetDecoderProperties2)decoder;
                
                    ByteBuffer properties = altCoderInfo.Properties;
                    int size = properties.GetCapacity();
                    if (size == -1) // (size > 0xFFFFFFFF)
                        return HRESULT.E_NOTIMPL;
                    if (size > 0) {
                        boolean ret = setDecoderProperties.SetDecoderProperties2(properties.data() /* , size */ );
                        if (ret == false) return HRESULT.E_FAIL;
                    }
                } catch (ClassCastException e) {
                    // nothing to do
                }
            }
            /*
            #ifdef COMPRESS_MT
                    if (mtMode) {
                CMyComPtr<ICompressSetCoderMt> setCoderMt;
                decoder.QueryInterface(IID_ICompressSetCoderMt, &setCoderMt);
                if (setCoderMt) {
                    RINOK(setCoderMt->SetNumberOfThreads(numThreads));
                }
                    }
            #endif
             
            #ifndef _NO_CRYPTO
             {
                CMyComPtr<ICryptoSetPassword> cryptoSetPassword;
                decoder.QueryInterface(IID_ICryptoSetPassword, &cryptoSetPassword);
                if (cryptoSetPassword) {
                    if (getTextPassword == 0)
                        return E_FAIL;
                    CMyComBSTR password;
                    RINOK(getTextPassword->CryptoGetTextPassword(&password));
                    CByteBuffer buffer;
                    UString unicodePassword(password);
                    const UInt32 sizeInBytes = unicodePassword.Length() * 2;
                    buffer.SetCapacity(sizeInBytes);
                    for (int i = 0; i < unicodePassword.Length(); i++) {
                        wchar_t c = unicodePassword[i];
                        ((Byte *)buffer)[i * 2] = (Byte)c;
                        ((Byte *)buffer)[i * 2 + 1] = (Byte)(c >> 8);
                    }
                    RINOK(cryptoSetPassword->CryptoSetPassword(
                            (const Byte *)buffer, sizeInBytes));
                }
            }
            #endif
             */
            coderIndex++;
            
            int numInStreams = coderInfo.NumInStreams;
            int numOutStreams = coderInfo.NumOutStreams;
            LongVector packSizesPointers = new LongVector(); // CRecordVector<const UInt64 *>
            LongVector unPackSizesPointers = new LongVector(); // CRecordVector<const UInt64 *>
            packSizesPointers.Reserve(numInStreams);
            unPackSizesPointers.Reserve(numOutStreams);
            int j;
            for (j = 0; j < numOutStreams; j++, unPackStreamIndex++)
                unPackSizesPointers.add(folderInfo.UnPackSizes.get(unPackStreamIndex));
            
            for (j = 0; j < numInStreams; j++, packStreamIndex++) {
                int bindPairIndex = folderInfo.FindBindPairForInStream(packStreamIndex);
                if (bindPairIndex >= 0)
                    packSizesPointers.add(
                            folderInfo.UnPackSizes.get(folderInfo.BindPairs.get(bindPairIndex).OutIndex));
                else {
                    int index = folderInfo.FindPackStreamArrayIndex(packStreamIndex);
                    if (index < 0)
                        return HRESULT.E_FAIL;
                    packSizesPointers.add(packSizes.get(index));
                }
            }
            
            _mixerCoderCommon.SetCoderInfo(i,
                    packSizesPointers, // &packSizesPointers.Front(),
                    unPackSizesPointers // &unPackSizesPointers.Front()
                    );
        }
        
        int [] temp_useless = new int [1]; // TBD
        int [] tmp1 = new int[1];
        bindInfo.FindOutStream(bindInfo.OutStreams.get(0), tmp1 /* mainCoder */ , temp_useless /* temp */);
        int mainCoder = tmp1[0];
        
        if (_multiThread) {
           // _mixerCoderMTSpec.SetProgressCoderIndex(mainCoder);
           throw new IOException("Multithreaded decoder is not implemented");
        }
        
        if (numCoders == 0)
            return 0;
        RecordVector<java.io.InputStream> inStreamPointers = new RecordVector<java.io.InputStream>(); // CRecordVector<ISequentialInStream *>
        inStreamPointers.Reserve(inStreams.size());
        for (i = 0; i < inStreams.size(); i++)
            inStreamPointers.add(inStreams.get(i));

        RecordVector<java.io.OutputStream> outStreamPointer = new RecordVector<java.io.OutputStream>();
        outStreamPointer.add(outStream);
        return _mixerCoder.Code(
                inStreamPointers, //&inStreamPointers.Front(),
                null,
                inStreams.size(),
                outStreamPointer, // &outStreamPointer,
                null, 1, compressProgress);
    }
}
