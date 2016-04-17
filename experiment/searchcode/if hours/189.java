// �Ĥ���:507-�����O�έp��
public class JPD507 {
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
    
    public static void park(int hours) {
        int[] hourTable = {0, 2, 4, 6};   // �ɬq
        int[] feeTable = {30, 50, 80, 100};   // �ɬq�O�v
        int fee = 0;   //�����O��
        
        System.out.println("�����ɼơG" + hours + "�p��");
        
        int i = hourTable.length - 1;
        while (i > 0) {   // ���X�̰��O�v�Ϭq
            if (hourTable[i] < hours)
                break;
            i--;
        }

        while (i >= 0) {   // �ѳ̰��O�v�Ϭq���U�֥[
            fee += (hours - hourTable[i]) * feeTable[i];
            hours = hourTable[i];
            i--;
        }

        System.out.println("��ú�O�ΡG" + fee + "����");
    }
}

