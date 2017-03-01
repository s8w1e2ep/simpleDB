package com.test.db;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

public interface Table extends Closeable {

	String getName();

	boolean insert(Cell cell) throws IOException;

	void delete(byte[] row) throws IOException;

	Iterator<Cell> get(byte[] row) throws IOException;

	Optional<Cell> get(byte[] row, byte[] column) throws IOException;

	boolean delete(byte[] row, byte[] column) throws IOException;

	boolean insertIfAbsent(Cell cell) throws IOException;
}
