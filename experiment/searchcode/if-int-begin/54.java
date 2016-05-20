package controller;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.table.DefaultTableModel;
import model.Teacher;
import model.Subject;
import model.Group;
import model.Group_Teacher_Subject;
import model.Interim;
import model.Lab;
import model.Lab_Student;
import model.Lesson;
import model.Skip;
import model.Student;
import model.query.HQL;

/**
 *
 * @author cska
 * 
 */

public class TeacherCtrl {

    protected Teacher teacher;
    protected List<Subject> subjects;
    protected List<List<Group>> groups;

    public TeacherCtrl(String login) {
        for (Teacher t : HQL.getSelect().teachers()) {
            if (login.equals(t.getLogin())) {
                this.teacher = t;
            }
        }
        subjects = new ArrayList<Subject>();
        groups = new ArrayList<List<Group>>();
        for (Group_Teacher_Subject gts : HQL.getSelect().gtses()) {
            if (teacher.getId().equals(gts.getTeacher().getId())) {
                if (subjects.contains(gts.getSubject())) {
                    int index = subjects.indexOf(gts.getSubject());
                    groups.get(index).add(gts.getGroup());
                } else {
                    List<Group> temp = new ArrayList<Group>();
                    temp.add(gts.getGroup());
                    groups.add(temp);
                    subjects.add(gts.getSubject());
                }
            }
        }
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public Subject getSubject(int index) {
        return subjects.get(index);
    }

    public Group getGroup(int sIndex, int index) {
        System.out.println(sIndex + " " + index);
        int i = 0;
        for (Group group : groups.get(sIndex))
            if (index == i) {
                return group;
            }
            else
                i++;
        return null;
    }

    public Lesson getLesson(int sIndex, int gIndex, int index, String begin, String end) {
        Subject subject = getSubject(sIndex);
        Group group = getGroup(sIndex, gIndex);
        int i = 0;
        for (Lesson lesson : HQL.getSelect().lessons())
            if (comp(begin, lesson.getDate()) != 1 && comp(lesson.getDate(),end) != 1)
            if (lesson.getSubject().getId().equals(subject.getId()))
                if (lesson.getGroup().getId().equals(group.getId())) {
                    if (index == i)
                        return lesson;
                    else
                        i++;
                }
        return null;
    }

    public Skip getSkip(int sIndex, int gIndex, int lIndex, int index, String begin, String end) {
        int i = 0;
        if (lIndex != -1) {
            Lesson lesson = getLesson(sIndex, gIndex, lIndex, begin, end);
            for (Skip skip : lesson.getSkips())
                if (index == i)
                    return HQL.getSelect().skip(skip.getId());
                else
                    i++;
        }
        return null;
    }

    public List<Student> getStudents(int sIndex, int gIndex, int lIndex, int index, String begin, String end) {
        List<Student> s = new ArrayList<Student>();
        Lesson lesson = getLesson(sIndex, gIndex, lIndex, begin, end);
        Group group = getGroup(sIndex, gIndex);
        if (index != -1) s.add(getSkip(sIndex, gIndex, lIndex, index, begin, end).getStudent());
        for (Student student : group.getStudents()) {
            boolean fl = true;
            for (Skip skip : lesson.getSkips())
                if (student.getId().equals(HQL.getSelect().skip(skip.getId()).getStudent().getId())) {
                    fl = false;
                    break;
                }
            if (fl) s.add(student);
        }
        return s;
    }

    public List<Student> getStudetns(int sIndex, int gIndex, int lIndex, String begin, String end) {
        Lab lab = getLab(sIndex, begin, end, lIndex);
        Group group = getGroup(sIndex, gIndex);
        List<Student> students = new ArrayList<Student>();
        for (Student student : group.getStudents()) {
            boolean fl = true;
            for (Lab_Student lab_Student : lab.getLs()) {
                Lab_Student ls = HQL.getSelect().ls(lab_Student.getId());
                if (student.getId().equals(ls.getStudent().getId())) {
                    fl = false;
                    break;
                }
            }
            if (fl)
                students.add(student);
        }
        return students;
    }

    public int getStudentIndex(Student student, int sIndex, int gIndex) {
        int index = 0;
        for (Student s : groups.get(sIndex).get(gIndex).getStudents())
            if (student.getId().equals(s.getId()))
                return index;
            else
                index++;
        return 0;
    }

    public Lab getLab(int sIndex, String begin, String end, int index) {
        int i = 0;
        for (Lab lab : HQL.getSelect().labs()) {
            if ((comp(begin, lab.getBegin_date()) != 1) && (comp(lab.getBegin_date(), end) != 1)) {
                if (lab.getSubject().getId().equals(subjects.get(sIndex).getId())) {
                    if (index == i)
                        return lab;
                    else
                        i++;
                }
            }
        }
        return null;
    }

    public Lab_Student getLs(Lab lab, Group group, int index) {
        int i = 0;
        for (Lab_Student ls : lab.getLs()) {
            Student student = HQL.getSelect().student(ls.getStudent().getId());
            if (group.getId().equals(student.getGroup().getId())) {
                if (index == i)
                    return HQL.getSelect().ls(ls.getId());
                else
                    i++;
            }
        }
        return null;
    }

    public Interim getInterim(int sIndex, int lIndex, int gIndex, int lsIndex, int index, String begin, String end) {
        Lab_Student ls = null;
        int i = 0;
        if (lsIndex != -1) {
            ls = getLs(getLab(sIndex, begin, end, lIndex), getGroup(sIndex, gIndex), lsIndex);
            for (Interim interim : ls.getInterims()) {
                if (i == index)
                    return interim;
                else
                    i++;
            }
        }
        return null;
    }

    public DefaultComboBoxModel subjectComboBox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (Subject subject : subjects) {
            model.addElement(subject.toString());
        }
        return model;
    }

    public DefaultComboBoxModel groupComboBox(int index) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (Group group : groups.get(index)) {
            model.addElement(group.toString());
        }
        return model;
    }

    public DefaultTableModel lessonTable(int sIndex, int gIndex, String begin, String end) {
        Subject subject = getSubject(sIndex);
        Group group = getGroup(sIndex, gIndex);
        String headers[] = {"????", "???", "????", "????"};
        DefaultTableModel model = new DefaultTableModel(headers,0);
        for (Lesson lesson : HQL.getSelect().lessons())
            if (comp(begin, lesson.getDate()) != 1 && comp(lesson.getDate(),end) != 1)
            if (lesson.getSubject().getId().equals(subject.getId()))
                if (lesson.getGroup().getId().equals(group.getId())) {
                    String type = "";
                    switch (lesson.getType()) {
                        case 0:
                            type += "???.";
                            break;
                        case 1:
                            type += "???.";
                            break;
                        case 2:
                            type += "????.";
                            break;
                    }
                    String row[] = {lesson.getDate(), type, lesson.getTheme(), lesson.getPlan()};
                    model.addRow(row);
                }
        return model;
    }

    public DefaultTableModel skipTable(int sIndex, int gIndex, int lIndex, String begin, String end) {
        String headers[] = {"???????", "?????? ???", "?????? ???", "???????"};
        DefaultTableModel model = new DefaultTableModel(headers, 0);
        if (lIndex != -1) {
            Lesson lesson = getLesson(sIndex, gIndex, lIndex, begin, end);
            for (Skip skip : lesson.getSkips()) {
                String first = skip.getFirst() == 0 ? "?? ???" : "???";
                String second = skip.getSecond() == 0 ? "?? ???" : "???";
                String cause = skip.getCause() == 0 ? "????." : "??.";
                Skip s = HQL.getSelect().skip(skip.getId());
                String row[] = {s.getStudent().getFio(), first, second, cause};
                model.addRow(row);
            }
        }
        return model;
    }

    public DefaultTableModel labTable(int sIndex, String begin, String end) {
        String headers[] = {"???? ??????", "???? ?????", "???????"};
        DefaultTableModel model = new DefaultTableModel(headers, 0);
        for (Lab lab : HQL.getSelect().labs()) {
            if ((comp(begin, lab.getBegin_date()) != 1) && (comp(lab.getBegin_date(), end) != 1)) {
                if (lab.getSubject().getId().equals(subjects.get(sIndex).getId())) {
                    String row[] = {lab.getBegin_date(), lab.getEnd_date(), lab.getText()};
                    model.addRow(row);
                }
            }
        }
        return model;
    }

    public DefaultComboBoxModel labBox(int sIndex, String begin, String end) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (Lab lab : HQL.getSelect().labs()) {
            if ((comp(begin, lab.getBegin_date()) != 1) && (comp(lab.getBegin_date(), end) != 1)) {
                if (lab.getSubject().getId().equals(subjects.get(sIndex).getId())) {
                    model.addElement(lab.getText());
                }
            }
        }
        return model;
    }

    public DefaultTableModel lab_studentTable(Lab lab, Group group) {
        String headers[] = {"???????", "???? ?????", "??????"};
        DefaultTableModel model = new DefaultTableModel(headers,0);
        for (Lab_Student ls : lab.getLs()) {
            Student student = HQL.getSelect().student(ls.getStudent().getId());
            if (group.getId().equals(student.getGroup().getId())) {
                String mark = "??? ??????";
                switch (ls.getMark()) {
                    case 0:
                        mark = "???????.";
                        break;
                    case 1:
                        mark = "?????.";
                        break;
                    case 2:
                        mark = "??????.";
                        break;
                    case 3:
                        mark = "????.";
                        break;
                    case 4:
                        mark = "???.";
                        break;
                    case 5:
                        mark = "???.";
                        break;
                }
                String date = ls.getReal_date().equals("") ? "??? ??????" : ls.getReal_date();
                String row[] = {student.getFio(), date, mark};
                model.addRow(row);
            }
        }
        return model;
    }

    public DefaultTableModel interimTable(int sIndex, int lIndex, int gIndex, String begin, String end, int index) {
        Lab_Student ls = null;
        if (index != -1)
            ls = getLs(getLab(sIndex, begin, end, lIndex), getGroup(sIndex, gIndex), index);
        String headers[] = {"????", "?????????"};
        DefaultTableModel model = new DefaultTableModel(headers,0);
        if (index != -1)
        for (Interim interim : ls.getInterims()) {
            String row[] = {interim.getDate(), interim.getMark()};
            model.addRow(row);
        }
        return model;
    }

    public Lesson addLesson(Lesson lesson) {
        return HQL.getInsert().lesson(lesson);
    }

    public void deleteLesson(Lesson lesson) {
        HQL.getDelete().lesson(lesson);
    }

    public void updateLesson(Lesson lesson) {
        HQL.getUpdate().lesson(lesson);
    }

    public void addSkips(Lesson lesson, List<Student> students) {
        for (Student student : students)
            HQL.getInsert().skip(new Skip(0, 0, lesson, student, 0));
    }

    public void addSkip(Skip skip) {
        HQL.getInsert().skip(skip);
    }

    public void deleteSkip(Skip skip) {
        HQL.getDelete().skip(skip);
    }

    public void updateSkip(Skip skip) {
        HQL.getUpdate().skip(skip);
    }

    public void addLab(Lab lab) {
        HQL.getInsert().lab(lab);
    }

    public void deleteLab(Lab lab) {
        HQL.getDelete().lab(lab);
    }

    public void updateLab(Lab lab) {
        HQL.getUpdate().lab(lab);
    }

    public void addLs(Lab lab, List<Student> studnets) {
        for (Student student : studnets)
            HQL.getInsert().ls(new Lab_Student(student, lab, "", 6));
    }

    public void deleteLs(Lab_Student ls) {
        HQL.getDelete().ls(ls);
    }

    public void updateLs(Lab_Student ls) {
        HQL.getUpdate().ls(ls);
    }

    public void addInterim(Interim interim) {
        HQL.getInsert().interim(interim);
    }

    public void deleteInterim(Interim interim) {
        HQL.getDelete().interim(interim);
    }

    public void updateInterim(Interim interim) {
        HQL.getUpdate().interim(interim);
    }

    public int comp(String date1, String date2) {
        String s1[] = date1.split("\\.");
        Integer y1 = Integer.parseInt(s1[0]);
        Integer m1 = Integer.parseInt(s1[1]);
        Integer d1 = Integer.parseInt(s1[2]);
        String s2[] = date2.split("\\.");
        Integer y2 = Integer.parseInt(s2[0]);
        Integer m2 = Integer.parseInt(s2[1]);
        Integer d2 = Integer.parseInt(s2[2]);
        if (y1 < y2) return -1;
        if (y1 > y2) return 1;
        if (m1 < m2) return -1;
        if (m1 > m2) return 1;
        if (d1 < d2) return -1;
        if (d1 > d2) return 1;
        return 0;
    }

}

