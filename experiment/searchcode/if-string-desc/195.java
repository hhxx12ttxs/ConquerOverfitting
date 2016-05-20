package org.lightwings.asm;

public class ASMMethodInfo {
    int access;
    String name;
    String desc;
    String signature;
    String methodKey;
    int maxStack;
    int maxLocals;
    String shortKey;

    public ASMMethodInfo(int access, String name, String desc, String signature) {
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.signature = signature;
        this.methodKey = genKey(access, name, desc, signature);

        StringBuilder buf = new StringBuilder();
        buf.append(name).append(".(");
        if (this.desc != null) {
            String[] paras = this.desc.split(";");
            if (paras != null) {
                for (int i = 0; i < paras.length; i++) {
                    int slashPos = paras[i].lastIndexOf("/");
                    if (slashPos > 0) {
                        paras[i] = paras[i].substring(slashPos + 1);
                    }
                    buf.append(paras[i]).append(",");
                }
                buf.deleteCharAt(buf.length() - 1);
            }
        }
        buf.append(")");
        shortKey = buf.toString();
    }

    public String getKey() {
        return methodKey;
    }

    public static String genKey(int access, String name, String desc, String signature) {
        return access + "^" + name + "^" + desc + "^" + signature;
    }

    public int getAccess() {
        return access;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getSignature() {
        return signature;
    }

    public int getMaxStack() {
        return maxStack;
    }

    public void setMaxStack(int maxStack) {
        this.maxStack = maxStack;
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public void setMaxLocals(int maxLocals) {
        this.maxLocals = maxLocals;
    }

    public String getShortKey() {
        return this.shortKey;
    }
}

