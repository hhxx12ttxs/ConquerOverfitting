public class ParkFeeIf {
  public static void main(String[] argv) {
    int hours = 0;
    int fee = 0;

    // �ഫ�� int
    hours = Integer.parseInt(argv[0]);

    if(hours > 6) { // ��p��W�L6�p�ɪ�����
            fee += (hours - 6) * 100;
            hours = 6;
    }

    if(hours > 4) { // �p��4~6�p�ɪ��ɬq
            fee += (hours - 4) * 80;
            hours = 4;
    }

    if(hours > 2) { // �p��2~4�p�ɪ��ɬq
            fee += (hours - 2) * 50;
            hours = 2;
    }

    if(hours > 0) { // �p��2�p�ɤ����ɬq
            fee += (hours - 0) * 30;
            hours = 0;
    }

    System.out.println("�����ɼơG" + argv[0] + "�p��");
    System.out.println("��ú�O�ΡG" + fee + "����");
  }
}

