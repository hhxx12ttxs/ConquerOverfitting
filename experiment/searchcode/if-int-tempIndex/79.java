/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package asnconverter;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author eduardo
 */
public class CAsnNode {

    private enum TNodeType {

        ntPrimitive, ntConstructed
    };

    private enum TNodeClass {

        ncUniversal, ncApplication, ncContext_Specific, ncPrivate
    };
    private TNodeType NodeType;
    private TNodeClass NodeClass;
    private byte TagNumber;
    private int dataSize;
    ArrayList<CAsnNode> SubNodes;
    char[] PrimitiveData;
    private int CurrentIndex;

    public int GetNumberOfBytes(int data) {
        int nBytes;

        nBytes = 0;
        while (data >= 1) {
            nBytes++;
            data /= 2;
        }
        if (nBytes % 8 == 0) {
            nBytes /= 8;
        } else {
            nBytes = nBytes / 8 + 1;
        }

        if (nBytes == 0) {
            nBytes = 1;
        }

        return nBytes;
    }

    void SetNodeType(byte nType) {
//        NodeType = (TNodeType) ((nType & 0x20) >> 5);
    }

    void SetNodeClass(byte nClass) {
//        NodeClass = (TNodeClass) ((nClass & 0xC0) >> 6);
    }

    void SetTagNumber(byte nTagNumber) {
        TagNumber = (nTagNumber & 0x1F);
    }

    TNodeType GetNodeType() {
        return NodeType;
    }

    TNodeClass GetNodeClass() {
        return NodeClass;
    }

    int GetTagNumber() {
        return TagNumber;
    }

    int GetHeaderSize() {
        int nodeSize;
        int result;

        nodeSize = GetSequenceSize();
        if (nodeSize <= 127) {
            result = 2;
        } else {
            result = 2 + GetNumberOfBytes(nodeSize);
        }
        return result;
    }

    int GetNodeSize() {
        return GetHeaderSize() + GetSequenceSize();
    }

    void logAsn(String logText) {

        char[] tmpLog;

        tmpLog = new char[logText.length() + 50];
        try {
            File file = new File("c:\\logs\\LogAsn\\logAsnNode.txt");

            if (!file.exists()) {
                file.mkdirs();
            }

        } catch (Exception e) {
        }
    }

    int ParseSequenceSize(byte[] sequence) {
        int sequenceSize;
        int lengthOctet;
        sequenceSize = 0;
        lengthOctet = sequence[1];
        if (lengthOctet < 128) {
            sequenceSize = lengthOctet;
        } else {
            lengthOctet = lengthOctet & 0x7F;
            for (int i = 2; i < (lengthOctet + 2); i++) {
                sequenceSize = (sequenceSize << 8) + sequence[i];
            }
        }
        return sequenceSize;
    }

    int GetSequenceSize() {
        int result;

        result = 0;
        if (NodeType == NodeType.ntPrimitive) {
            result = dataSize;
        } else {
            for (int i = 0; i < SubNodes.size(); i++) {
                CAsnNode ptrNode = (CAsnNode) SubNodes.get(i);
                result += ptrNode.GetNodeSize();
            }
        }
        return result;
    }

    //char[] GetSequence(charo[] MArg, int dataSize = 10) {
    byte[] GetSequence(byte[] MArg, int dataSize) {
        try {
            int LengthOctet;
            int NumLengthOctets;

            byte[] M = MArg;

            if (dataSize >= 2) {
                LengthOctet = M[1];
                if (LengthOctet == 0) {
                    return M[0];
                }
                if (LengthOctet <= 127) {
                    if (dataSize >= 3) {
                        return M[2];
                    }
                } else if (LengthOctet == 128) {
                    if (dataSize >= 3) {
                        return M[2];
                    }
                } else {
                    NumLengthOctets = LengthOctet - 128;
                    if (dataSize > (NumLengthOctets + 2)) {
                        return M[NumLengthOctets + 2];
                    }
                }
            }
        } catch (Exception e) {
            
        }
        return MArg[0];

    }

    String RetornoHex(char[] Mensagem, int t) {
        try {
            String S = "";
            String S1 = "'";

            for (int k = 0; k < t; k++) {
                if (k > 0) {
                    System.out.println(" %.2X" + Mensagem[k]);
                } else {
                    System.out.println("%.2X" + Mensagem[k]);
                }
                S = S + " ";
                S1 += S;
            }
            S1 += "'H";

            return S1;

        } catch (Exception e) {
        }

        return "";
    }

    String ClassText(TNodeClass nClass) {
        switch (nClass) {
            case ncUniversal:
                return "Universal";
            case ncApplication:
                return "Application";
            case ncContext_Specific:
                return "Context_Specific";
            case ncPrivate:
                return "Private";
        }
        return "???";
    }

    CAsnNode GetSpecificSequence(TNodeClass nClass, int tNumber) {
        try {
            CAsnNode tempNode;
            for (int i = CurrentIndex; i < SubNodes.size(); i++) {
                tempNode = (CAsnNode) SubNodes.get(i);
                if (tempNode.NodeClass == nClass) {
                    if (tempNode.TagNumber == tNumber) {
                        CurrentIndex = i + 1;
                        return tempNode;
                    }
                }
            }

        } catch (Exception e) {
            return null;
        }
        return null;
    }

    CAsnNode GetNextSequence() {
        try {
            CAsnNode tempNode;
            if (CurrentIndex < SubNodes.size()) {
                tempNode = (CAsnNode) SubNodes.get(CurrentIndex);
                CurrentIndex++;
                return tempNode;
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    int GetIntegerData() {
        int result = 0;
        if (NodeType == TNodeType.ntPrimitive) {
            for (int i = 0; i < dataSize && dataSize < 5; i++) {
                result = (result << 8) | PrimitiveData[i];
            }
        }
        return result;
    }

    String GetStringData() {
        String result = "";
        if (NodeType == TNodeType.ntPrimitive) {
            for (int i = 0; i < dataSize; i++) {
                result += (char) PrimitiveData[i];
            }
        }
        return result;
    }

    //Procurar por memory leaks
    char[] c_str() {
        char[] tempBuffer;
        tempBuffer = new char[GetNodeSize()];
        tempBuffer[0] = 0;
        switch (NodeClass) {
            case ncUniversal:
                tempBuffer[0] += 0x00;
                break;
            case ncApplication:
                tempBuffer[0] += 0x40;
                break;
            case ncContext_Specific:
                tempBuffer[0] += 0x80;
                break;
            case ncPrivate:
                tempBuffer[0] += 0xC0;
                break;
        }

        int tempIndex = 2;
        if (TagNumber <= 30) {
            tempBuffer[0] += TagNumber;
        }

        if (GetSequenceSize() <= 127) {
            tempBuffer[1] = (char) GetSequenceSize();
        } else {
            int nBytes = GetNumberOfBytes(GetSequenceSize());
            int tempSize = GetSequenceSize();

            tempBuffer[1] = (char) (0x80 + nBytes);

            for (int i = 0; i < nBytes; i++) {
                tempBuffer[1 + nBytes - i] = (char) (tempSize & 0xFF);
                tempSize = tempSize >> 8;
                tempIndex++;
            }
        }

        if (NodeType == TNodeType.ntPrimitive) {

            char[] temp = Utils.concatArray(tempBuffer, tempIndex, PrimitiveData, 0, dataSize);

            for (int i = 0; i < temp.length; i++) {
                tempBuffer[i + tempIndex] = temp[i];
            }

            //memcpy(&  tempBuffer[tempIndex], PrimitiveData , dataSize);

        } else {

            tempBuffer[0] += 0x20;

            for (int iList = 0; iList < SubNodes.size(); iList++) {
                CAsnNode tempNode;
                tempNode = (CAsnNode) SubNodes.get(iList);

                char[] tempMessage = tempNode.c_str();

                //memcpy(&  tempBuffer[tempIndex], tempMessage, tempNode ->  GetNodeSize());
                Utils.concatArray(tempBuffer, tempIndex, tempMessage, 0,
                        tempNode.GetNodeSize());

                tempIndex += tempNode.GetNodeSize();


            }
        }

        return tempBuffer;
    }

    //CAsnNode AddInv(__int64 pInt64Data
    CAsnNode AddInv(long pInt64Data) {
        long tempData;
        long byteMask;
        int nBytes;

        tempData = pInt64Data;
        nBytes = 0;
        while (tempData >= 1) {
            nBytes += 1;
            tempData /= 2;
        }
        if (nBytes % 8 == 0) {
            nBytes /= 8;
        } else {
            nBytes = nBytes / 8 + 1;
        }

        PrimitiveData = new char[nBytes];
        dataSize = nBytes;

        for (int i = 0; i < nBytes; i++) {
            byteMask = (long) (0xFF * Math.pow(2, (8 * i)));
            PrimitiveData[i] = (char) ((pInt64Data & byteMask) >> (i * 8));
        }
        return this;
    }


    
//    CAsnNode Add(__int64 pInt64Data
    CAsnNode Add(long pInt64Data) {
                            long tempData;
                            long byteMask;
        int nBytes;

        tempData = pInt64Data;
        nBytes = 0;
        while (tempData >= 1) {
            nBytes += 1;
            tempData /= 2;
        }
        if (nBytes % 8 == 0) {
            nBytes /= 8;
        } else {
            nBytes = nBytes / 8 + 1;
        }

        if (nBytes == 0) {
            nBytes = 1;
        }

        PrimitiveData = new char[nBytes];
        dataSize = nBytes;


        for (int i = 0; i < nBytes; i++) {
            byteMask = (char) (0xFF * Math.pow(2, (8 * i)));
            PrimitiveData[nBytes - i - 1] = (char) ((pInt64Data & byteMask) >> (i * 8));
        }
        return this;
    }


    
    CAsnNode Add(long pInt64Data, int nBytes) {
                long tempData;
                long byteMask;

        tempData = pInt64Data;

        PrimitiveData = new char[nBytes];
        dataSize = nBytes;

        for (int i = 0; i < nBytes; i++) {
            byteMask = (char) (0xFF * Math.pow(2, (8 * i)));
            PrimitiveData[nBytes - i - 1] = (char) ((pInt64Data & byteMask) >> (i * 8));
        }
        return this;
    }



    CAsnNode Add(char[] data, int nBytes) {
                PrimitiveData = new char[nBytes];
                dataSize = nBytes;


                for(int i = 0; i < nBytes; i++) {
            PrimitiveData[i] = data[i];
        }
        return this;
    }


    CAsnNode Add(char[] pStringData) {
            int strSize;

            strSize = pStringData.length;
            dataSize = strSize;
            
            PrimitiveData = new char[strSize];

            for (int iStr = 0, iData = 0; iStr < strSize; iStr++, iData++) {
            PrimitiveData[iData] = pStringData[iStr];
        }
        return this;
    }


    CAsnNode Add(CAsnNode cAsnNode) {
        SubNodes.add(cAsnNode);
        return this;
    }


    String LogText(int logNivel) {
        String returnText = "";
        String tempTab = "";
        int SequenceSize;

        for (int tNivel = 0; tNivel < logNivel; tNivel++) {
            tempTab += "\t";
        }
        if (NodeType == TNodeType.ntConstructed) {
            returnText += tempTab + ClassText(NodeClass) + " " + (TagNumber) + "\n";
            returnText += tempTab + "{\n";

            /**/
            for (int i = 0; i < SubNodes.size(); i++) {
                CAsnNode tempSubNode;
                tempSubNode = (CAsnNode) SubNodes.get(i);
                returnText += tempSubNode.LogText(logNivel + 1);
            }

            returnText += tempTab + "}\n";
        } else if (GetNodeType() == TNodeType.ntPrimitive) {
            String s = "";

            if (NodeClass == TNodeClass.ncUniversal && TagNumber == 4 && 
                    NodeType == TNodeType.ntPrimitive) {
                SequenceSize = GetSequenceSize();
                for (int a = 0; a < SequenceSize; a++) {
                    s += (char) PrimitiveData[a];
                }
            }
            
            returnText += tempTab + ClassText(NodeClass) + " " + (TagNumber) +
                    " = " + (GetSequenceSize() > 0 ? RetornoHex(PrimitiveData,
                    GetSequenceSize()) : ("(NULL)")) + (s.length() > 0 ?
                            ("(" + s + ")") : (""));
            returnText += "\n";
        }
        return returnText;
    }

    void Rewind() {
        CurrentIndex = 0;
    }


    int ParseHeaderSize(byte[] sequence)
        {
            int headerSize;
            int lengthOctet;
            headerSize = 0;

        lengthOctet = sequence[1];

        if (lengthOctet <= 127) {
            headerSize = 2;
        } else {
            lengthOctet = lengthOctet & 0x7F;
            headerSize = 2 + lengthOctet;
        }

        return headerSize;
    }


    CAsnNode(byte[] Sequence)
        {
        SubNodes = new ArrayList<CAsnNode>();
        SetNodeType(Sequence[0]);
        SetNodeClass(Sequence[0]);
        SetTagNumber(Sequence[0]);

        CurrentIndex = 0;

        if (NodeType == TNodeType.ntConstructed) {
            int TempDataSize;
            TempDataSize = ParseSequenceSize(Sequence);
            Sequence = GetSequence(Sequence, Sequence.length);
            while (TempDataSize > 0) {
                CAsnNode * newNode = new CAsnNode(Sequence);
                SubNodes->Add(newNode);
                TempDataSize -= newNode ->  GetNodeSize();
                Sequence = &  Sequence[newNode ->  GetNodeSize()];
            }
        } else if (GetNodeType() == ntPrimitive) {
            dataSize = ParseSequenceSize(Sequence);
            try {
                PrimitiveData = new char[dataSize];
                Sequence = GetSequence(Sequence);
                memcpy(PrimitiveData, Sequence, dataSize);
            } catch (Exception



                &e)
                {

                    logAsn(AnsiString("Erro: " + e.Message).c_str());
                }

            }
        }

        CAsnNode(unsigned



        char



        * sequence,
        unsigned



        int messageSize
                            )







        :
            NodeType(ntPrimitive),
            NodeClass(ncUniversal),
            TagNumber(0)
                            ,
            dataSize(0)
                            ,
            CurrentIndex(0)
        {

            unsigned



            int offset;
            unsigned



            int nodeType;
            unsigned



            int nodeClass;
            unsigned



            int tagNumber;

            unsigned



            int sequenceSize;
            unsigned



            int headerSize;

            SubNodes = new TList;

            CAsnNode * ptrNode;

            offset = 0;

            try {
                if (messageSize >= 2) {

                    nodeType = (*  sequence & 0x20) >> 5;
                    nodeClass = (*  sequence & 0xC0) >> 6;
                    tagNumber = *  sequence & 0x1F;

                    NodeType = (TNodeType) nodeType;
                    NodeClass = (TNodeClass) nodeClass;
                    TagNumber = tagNumber;
                    headerSize = ParseHeaderSize(sequence);

                    if (messageSize >= headerSize) {
                        sequenceSize = ParseSequenceSize(sequence);

                        if (messageSize >= (headerSize + sequenceSize)) {
                            if (NodeType == ntPrimitive) {
                                dataSize = sequenceSize;
                                PrimitiveData = new char[sequenceSize];
                                memcpy(PrimitiveData, &  sequence[headerSize], sequenceSize);
                            } else {
                                while (offset < sequenceSize) {
                                    ptrNode = new CAsnNode(&  sequence[headerSize + offset], sequenceSize - offset);
                                    offset += ptrNode ->  GetNodeSize();
                                    SubNodes->Add(ptrNode);
                                }
                            }
                        }
                    }
                }
            } catch (Exception



                &e)
                {
                    logAsn(AnsiString("Erro: " + e.Message).c_str());
                }
            }

            CAsnNode(TNodeClass



            nClass,
            TNodeType nType,



            int tNumber
















                  )
        {
                NodeClass = nClass;
                NodeType = nType;
                TagNumber = tNumber;
                dataSize = 0;
                CurrentIndex = 0;
                SubNodes = new TList;
            }

            ~CAsnNode()
        {




                CAsnNode * tempNode;
                for (int i = 0; i < SubNodes ->  Count; i++) {
                    tempNode = (CAsnNode * ) SubNodes ->  Items[i];
                    delete tempNode;
                }
                delete SubNodes;

                if (NodeType == ntPrimitive && (dataSize > 0)) {
                    delete



                }
                PrimitiveData;
            }

        }
        ;
        #
        endif
    
    





    }

