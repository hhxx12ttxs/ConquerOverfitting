/*
 * Instruction.java
 *
 * Created on March 18, 1999
 *
 * Modification Log:
 * 1.00  18th Mar 1999   Tanmay   Original version.
 * 1.01  11th May 2002   Tanmay   Made members public for use in view
 *-----------------------------------------------------------------------------------------
 *       10th Sep 2003   Tanmay   Moved to SourceForge (http://classeditor.sourceforge.net)
 *-----------------------------------------------------------------------------------------
 */


package classfile.attributes;

import classfile.ConstantPool;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Class to represent one instruction.
 *
 * @author 	    Tanmay K. Mohapatra
 * @version     1.01, 11th May, 2002
 */

public class Instruction {
    public int     iInstruction;
    public int     iDataLength;
    public int[]   aiData = new int[4];
    public int[]   aiLen = new int[4];
    public int     iNumData;
    public int     iPaddingLength, iDefaultByte, iNPairs, iLowInt, iHighInt;
    public int[]   aiMatchPairs, aiOffsetPairs, aiDefault;
    
    void readInstruction(DataInputStream dis, ConstantPool constPool, int iOffset) throws IOException {
        iInstruction = dis.readUnsignedByte();
        readInstructionData(dis, constPool, iInstruction, iOffset+1);
    }
    
    void writeInstruction(DataOutputStream dos, ConstantPool constPool) throws IOException {
        dos.writeByte(iInstruction);
        writeInstructionData(dos, constPool, iInstruction);
    }
    
    private void writeInstructionData(DataOutputStream dos, ConstantPool constPool, int iInstruction) throws IOException {
        if (0xab == iInstruction) {
            for (int iIndex=0; iIndex < iPaddingLength; iIndex++) {
                dos.writeByte(0);
            }
            dos.writeInt(iDefaultByte);
            dos.writeInt(iNPairs);
            for(int iIndex=0; iIndex < iNPairs; iIndex++) {
                dos.writeInt(aiMatchPairs[iIndex]);
                dos.writeInt(aiOffsetPairs[iIndex]);
            }
        }
        else if (0xaa == iInstruction) {
            for (int iIndex=0; iIndex < iPaddingLength; iIndex++) {
                dos.writeByte(0);
            }
            dos.writeInt(iDefaultByte);
            dos.writeInt(iLowInt);
            dos.writeInt(iHighInt);
            for (int iIndex=0; iIndex < iHighInt-iLowInt+1; iIndex++) {
                dos.writeInt(aiOffsetPairs[iIndex]);
            }
        }
        else {
            for (int iIndex=0; iIndex < iNumData; iIndex++) {
                switch(aiLen[iIndex]) {
                    case 1:
                        dos.writeByte(aiData[iIndex]);
                        break;
                    case 2:
                        dos.writeShort(aiData[iIndex]);
                        break;
                    case 4:
                        dos.writeInt(aiData[iIndex]);
                        break;
                }
            }
        }
    }
    
    private void readInstructionData(DataInputStream dis, ConstantPool constPool, int iInstruction, int iOffset) throws IOException {
        switch(iInstruction) {
            case 0x32:
            case 0x53:
            case 0x1:
            case 0x2a:
            case 0x2b:
            case 0x2c:
            case 0x2d:
            case 0xb0:
            case 0xbe:
            case 0x4b:
            case 0x4c:
            case 0x4d:
            case 0x4e:
            case 0xbf:
            case 0x33:
            case 0x54:
            case 0x34:
            case 0x55:
            case 0x90:
            case 0x8e:
            case 0x8f:
            case 0x63:
            case 0x31:
            case 0x52:
            case 0x98:
            case 0x97:
            case 0xe:
            case 0xf:
            case 0x6f:
            case 0x26:
            case 0x27:
            case 0x28:
            case 0x29:
            case 0x6b:
            case 0x77:
            case 0x73:
            case 0xaf:
            case 0x47:
            case 0x48:
            case 0x49:
            case 0x4a:
            case 0x67:
            case 0x59:
            case 0x5a:
            case 0x5b:
            case 0x5c:
            case 0x5d:
            case 0x5e:
            case 0x8d:
            case 0x8b:
            case 0x8c:
            case 0x62:
            case 0x30:
            case 0x51:
            case 0x96:
            case 0x95:
            case 0xb:
            case 0xc:
            case 0xd:
            case 0x6e:
            case 0x22:
            case 0x23:
            case 0x24:
            case 0x25:
            case 0x6a:
            case 0x76:
            case 0x72:
            case 0xae:
            case 0x43:
            case 0x44:
            case 0x45:
            case 0x46:
            case 0x66:
            case 0x91:
            case 0x92:
            case 0x87:
            case 0x86:
            case 0x85:
            case 0x93:
            case 0x60:
            case 0x2e:
            case 0x7e:
            case 0x4f:
            case 0x2:
            case 0x3:
            case 0x4:
            case 0x5:
            case 0x6:
            case 0x7:
            case 0x8:
            case 0x6c:
            case 0x1a:
            case 0x1b:
            case 0x1c:
            case 0x1d:
            case 0x68:
            case 0x74:
            case 0x80:
            case 0x70:
            case 0xac:
            case 0x78:
            case 0x7a:
            case 0x3b:
            case 0x3c:
            case 0x3d:
            case 0x3e:
            case 0x64:
            case 0x7c:
            case 0x82:
            case 0x8a:
            case 0x89:
            case 0x88:
            case 0x61:
            case 0x2f:
            case 0x7f:
            case 0x50:
            case 0x94:
            case 0x9:
            case 0xa:
            case 0x1e:
            case 0x1f:
            case 0x20:
            case 0x21:
            case 0x69:
            case 0x75:
            case 0x81:
            case 0x71:
            case 0xad:
            case 0x79:
            case 0x7b:
            case 0x3f:
            case 0x40:
            case 0x41:
            case 0x42:
            case 0x65:
            case 0x7d:
            case 0x83:
            case 0xc2:
            case 0xc3:
            case 0x0:
            case 0x57:
            case 0x58:
            case 0xb1:
            case 0x35:
            case 0x56:
            case 0x5f:
            case 0x6d:
                iNumData = 0;
                iDataLength = 0;
                break;
            case 0x19:
            case 0x3a:
            case 0x10:
            case 0x18:
            case 0x39:
            case 0x17:
            case 0x38:
            case 0x36:
            case 0x15:
            case 0x12:
            case 0x16:
            case 0x37:
            case 0xbc:
            case 0xa9:
                iNumData = 1;
                iDataLength = 1;
                aiLen[0] = 1;
                aiData[0] = dis.readUnsignedByte();
                break;
            case 0xbd:
            case 0xc0:
            case 0xb4:
            case 0xb2:
            case 0xa7:
            case 0xa5:
            case 0xa6:
            case 0x9f:
            case 0xa0:
            case 0xa1:
            case 0xa2:
            case 0xa3:
            case 0xa4:
            case 0x99:
            case 0x9a:
            case 0x9b:
            case 0x9c:
            case 0x9d:
            case 0x9e:
            case 0xc7:
            case 0xc6:
            case 0xc1:
            case 0xb7:
            case 0xb8:
            case 0xb6:
            case 0xa8:
            case 0x13:
            case 0x14:
            case 0xbb:
            case 0xb5:
            case 0xb3:
            case 0x11:
                iNumData = 1;
                iDataLength = 2;
                aiLen[0] = 2;
                aiData[0] = dis.readUnsignedShort();
                break;
            case 0x84:
                iNumData = 2;
                iDataLength = 2;
                aiLen[0] = 1;
                aiLen[1] = 1;
                aiData[0] = dis.readUnsignedByte();
                aiData[1] = dis.readUnsignedByte();
                break;
            case 0xc5:
                iNumData = 2;
                iDataLength = 3;
                aiLen[0] = 2;
                aiLen[1] = 1;
                aiData[0] = dis.readUnsignedShort();
                aiData[1] = dis.readUnsignedByte();
                break;
            case 0xc8:
            case 0xc9:
                iNumData = 1;
                iDataLength = 4;
                aiLen[0] = 4;
                aiData[0] = dis.readInt();
                break;
            case 0xb9:
                iNumData = 3;
                iDataLength = 4;
                aiLen[0] = 2;
                aiLen[1] = 1;
                aiLen[2] = 1;
                aiData[0] = dis.readUnsignedShort();
                aiData[1] = dis.readUnsignedByte();
                aiData[2] = dis.readUnsignedByte();
                break;
            case 0xab:
                iPaddingLength = iOffset%4;
                if (0 < iPaddingLength) iPaddingLength = 4-iPaddingLength;
                for (int iIndex=0; iIndex < iPaddingLength; iIndex++) {
                    dis.readUnsignedByte();
                }
                iDefaultByte = dis.readInt();
                iNPairs = dis.readInt();
                iDataLength = (iPaddingLength+8);
                if (iNPairs > 0) {
                    aiMatchPairs = new int[iNPairs];
                    aiOffsetPairs = new int[iNPairs];
                    for(int iIndex=0; iIndex < iNPairs; iIndex++) {
                        aiMatchPairs[iIndex] = dis.readInt();
                        aiOffsetPairs[iIndex] = dis.readInt();
                        iDataLength += 8;
                    }
                }
                break;
            case 0xaa:
                iPaddingLength = iOffset%4;
                if (0 < iPaddingLength) iPaddingLength = 4-iPaddingLength;
                for (int iIndex=0; iIndex < iPaddingLength; iIndex++) {
                    dis.readUnsignedByte();
                }
                iDefaultByte = dis.readInt();
                iLowInt = dis.readInt();
                iHighInt = dis.readInt();
                iDataLength = (iPaddingLength+12);
                if (iHighInt-iLowInt+1 > 0) {
                    aiOffsetPairs = new int[iHighInt-iLowInt+1];
                    for (int iIndex=0; iIndex < iHighInt-iLowInt+1; iIndex++) {
                        aiOffsetPairs[iIndex] = dis.readInt();
                        iDataLength += 4;
                    }
                }
                break;
            case 0xc4:
                aiLen[0] = 1;
                aiData[0] = dis.readUnsignedByte();
                if (0x84 == aiData[0]) {
                    iNumData = 3;
                    iDataLength = 5;
                    aiData[1] = dis.readUnsignedShort();
                    aiData[2] = dis.readUnsignedShort();
                    aiLen[1] = aiLen[2] = 2;
                }
                else {
                    iNumData = 2;
                    iDataLength = 3;
                    aiData[1] = dis.readUnsignedShort();
                    aiLen[1] = 2;
                }
                break;
            default:
                break;
        }
    }
    
    public String toString() {
        String sRet;
        sRet = Instructions.sInstrCodes[iInstruction];
        
        if (0xab == iInstruction) {
            sRet += " pad=" + iPaddingLength + " def=" + iDefaultByte + ", " + iNPairs;
            for (int iIndex=0; iIndex < iNPairs; iIndex++) {
                sRet += ", " + aiMatchPairs[iIndex] + ":" + aiOffsetPairs[iIndex];
            }
        }
        else if (0xaa == iInstruction) {
            sRet += " pad=" + iPaddingLength + " def=" + iDefaultByte + " " + iLowInt + " to " + iHighInt;
            for (int iIndex=0; iIndex < iHighInt-iLowInt+1; iIndex++) {
                sRet += " : " + aiOffsetPairs[iIndex];
            }
        }
        else {
            for (int iIndex=0; iIndex < iNumData; iIndex++) {
                sRet += " " + aiData[iIndex];
                if ((iIndex+1) < iNumData) {
                    sRet += ",";
                }
            }
        }
        return sRet;
    }
}

