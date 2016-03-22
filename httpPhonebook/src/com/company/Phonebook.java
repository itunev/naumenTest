package com.company;

import java.io.*;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Phonebook {
    private static TreeSet<Line> numberList;
    private static String fileName;
    private static Lock mutex;

    public class Line  implements Comparable<Object> {
        String	name;
        String	number;

        public Line(String name, String number)
        {
            this.name   = name;
            this.number = number;
        }

        @Override
        public int compareTo(Object obj)
        {
            Line entry	= (Line) obj;

            int	cmp		= name.compareTo(entry.name);
            if (cmp > 0)
                return	1;

            if (cmp < 0)
                return	-1;

            cmp			= number.compareTo(entry.number);
            if (cmp > 0)
                return	1;

            if (cmp < 0)
                return	-1;

            return		0;
        }
    }

    public Phonebook(String fileName) {
        this.fileName   = fileName;
        numberList      = new TreeSet<Line>();
        mutex           = new ReentrantLock(true);

        LoadList(fileName);
    }

    private void LoadList(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();

            while (line != null) {
                String nemeNumber[] = line.split("\\\\");
                numberList.add(new Line(nemeNumber[0], nemeNumber[1]));
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SaveList() {
        mutex.lock();

        try {
            // if file doesnt exists, then create it
            File file = new File(fileName);
            if (!file.exists())
                file.createNewFile();

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (Line line : numberList)
                bw.write(line.name + "\\" + line.number + "\r\n");

            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mutex.unlock();
        }
    }

    public boolean AddLine(String name, String number) {
        mutex.lock();

        boolean notExists = numberList.add(new Line(name, number));

        if (notExists)
            SaveList();

        mutex.unlock();

        return notExists;
    }

    public boolean DeleteLine(String name, String number) {
        mutex.lock();

        boolean deleted = numberList.remove(new Line(name, number));

        if (deleted)
            SaveList();

        mutex.unlock();

        return deleted;
    }

    public TreeSet<Line> GetListOfNumbers(String name) {
        mutex.lock();

        if (numberList == null) {
            mutex.unlock();

            return null;
        }

        Line higherLine = numberList.higher(new Line(name, ""));
        Line lowerLine = numberList.lower(new Line(name + "zzzzzzzzzzzzzzzzzzzzzz", ""));

        if(higherLine == null || lowerLine == null) {
            mutex.unlock();

            return null;
        }

        if (higherLine.name.compareTo(lowerLine.name) > 0) {
            mutex.unlock();

            return null;
        }

        TreeSet<Line> subList = (TreeSet<Line>) ((TreeSet<Line>)numberList.subSet(higherLine, true, lowerLine, true)).clone();

        mutex.unlock();

        return subList;
    }

//    private class PhoneLinesSort implements Comparator<String>
//    {
//
//        private boolean ascending;
//
//        PhoneLinesSort(boolean ascending)
//        {
//            this.ascending = ascending;
//        }
//
//
//        @SuppressWarnings("unchecked")
////        @Override
//        public int compare(String o1, String o2) {
//            if (ascending) {
//                return (o1 == null) ? -1 : ((o2 == null) ? 1 : (o1).compareTo(o2));
//            } else {
//                return (o1 == null) ? 1 : ((o2 == null) ? -1 : (o2).compareTo(o1));
//            }
//        }
//
//    }
}
