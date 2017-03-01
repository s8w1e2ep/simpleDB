package com.test.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;

public class Main {
	
	private static final byte[] R0 = BytesUtil.toBytes("a2");
	private static final byte[] C0 = BytesUtil.toBytes("name");
	private static final byte[] V0 = BytesUtil.toBytes("admin");
	
	private static final byte[] R1 = BytesUtil.toBytes("a1");
	private static final byte[] C1 = BytesUtil.toBytes("name");
	private static final byte[] V1 = BytesUtil.toBytes("guest");
	
	public static void main(String[] args) {
		try {
			File f = new File("admin.impl");
			if(!f.exists()){
				throw new Exception("admin.impl is not exist.");
			}
			FileInputStream fis = new FileInputStream("admin.impl");
			Properties prop = new Properties();
			prop.load(fis);
			Admin admin = new SimpleAdmin(prop);
			// open table
			if(!admin.tableExist("test")){
				admin.createTable("test");	
			}
			Table t = admin.openTable("test");
			// insert data
			Cell c = Cell.createCell(R0, C0, V0);
			Cell c1 = Cell.createCell(R1, C1, V1);
			if (t.insertIfAbsent(c)) {
				System.out.println("Insert a2-name Success");
			} else {
				System.out.println("a2-name is existing.");
			}
			if (t.insertIfAbsent(c)) {
				System.out.println("Insert a2-name Success");
			} else {
				System.out.println("a2-name is existing.");
			}
			if (t.insertIfAbsent(c1)) {
				System.out.println("Insert a1-name Success");
			} else {
				System.out.println("a1-name is existing.");
			}
			// list row
			for(Iterator<?> iter = t.get(R0); iter.hasNext();) {
				System.out.println(iter.next().toString());
			}
			// get cell
			Optional<?> op = t.get(R0, C0);
			if (op.isPresent()) {
				System.out.println(op.get().toString());
			} else {
				System.out.println("Not found a1-name");
			}
			// delete cell
			if (t.delete(R0, C0)) {
				System.out.println("Delete c1 success");
			} else {
				System.out.println("Delete fail");
			}
			// list row
			System.out.println("R0");
			for(Iterator<?> iter = t.get(R0); iter.hasNext();) {
				System.out.println(iter.next().toString());
			}
			System.out.println("R1");
			for(Iterator<?> iter = t.get(R1); iter.hasNext();) {
				System.out.println(iter.next().toString());
			}
			t.close();
			admin.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
