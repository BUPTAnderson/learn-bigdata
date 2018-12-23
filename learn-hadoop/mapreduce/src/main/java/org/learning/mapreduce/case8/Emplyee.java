package org.learning.mapreduce.case8;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class Emplyee implements WritableComparable<Emplyee> {
    private String empno = "";
    private String ename = "";
    private String deptno = "";
    private String dname = "";
    private int flag = 0; // 0: 部门  1：员工

    public Emplyee() {
    }

    public Emplyee(String empno, String ename, String deptno, String dname, int flag) {
        this.empno = empno;
        this.ename = ename;
        this.deptno = deptno;
        this.dname = dname;
        this.flag = flag;
    }

    public Emplyee(Emplyee emplyee) {
        this.empno = emplyee.getEmpno();
        this.ename = emplyee.getEname();
        this.deptno = emplyee.getDeptno();
        this.dname = emplyee.getDname();
        this.flag = emplyee.getFlag();
    }

    public String getEmpno() {
        return empno;
    }

    public void setEmpno(String empno) {
        this.empno = empno;
    }

    public String getEname() {
        return ename;
    }

    public void setEname(String ename) {
        this.ename = ename;
    }

    public String getDeptno() {
        return deptno;
    }

    public void setDeptno(String deptno) {
        this.deptno = deptno;
    }

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Override
    public int compareTo(Emplyee o) {
        return 0;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeUTF(empno);
        out.writeUTF(ename);
        out.writeUTF(deptno);
        out.writeUTF(dname);
        out.writeInt(flag);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.empno = in.readUTF();
        this.ename = in.readUTF();
        this.deptno = in.readUTF();
        this.dname = in.readUTF();
        this.flag = in.readInt();
    }

    @Override
    public String toString() {
        return this.empno + "\t" + this.ename + "\t" + this.dname + "\t" + this.deptno;
    }
}
