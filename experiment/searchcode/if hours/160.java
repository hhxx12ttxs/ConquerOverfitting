public class JPA507 {
    public static void main(String[] argv) {
        int hours = 0;   //�����ɼ�
        hours = 2;
        park(hours);
        System.out.println("--------------------");
        hours = 3;
        park(hours);
        System.out.println("--------------------");
        hours = 5;
        park(hours);
        System.out.println("--------------------");
        hours = 8;
        park(hours);
    }
    //�p�ⰱ���O�Τ�k
    public static void park(int hours) {
        int[] hourTable = {0, 2, 4, 6};   //�ɬq�}�C
        int[] feeTable = {30, 50, 80, 100};   //�ɬq�O�v�}�C
        int fee = 0;   //�`�����O��
        System.out.println("�����ɼơG" + hours + "�p��");
        for(int a = 3 ;a>=0;a--)
        {
        //�z�L�j�骺�覡�A�ϥκ|�檺��k�A�Y�ŦX���h�i�Jif�P�_��	
        if(hours>hourTable[a])
        	{
        	//�p����B�C�쥻��+(�ɼ�-�D�ݩ󦹮ɬq���ɶ�)*�Ӯɬq�O�v
        	fee=fee+(hours-hourTable[a])*feeTable[a];
        	//�]�w�ѤU���ɶ�
        	hours=hourTable[a];
        	}
        }	
        System.out.println("��ú�O�ΡG" + fee + "����");
    }
}
