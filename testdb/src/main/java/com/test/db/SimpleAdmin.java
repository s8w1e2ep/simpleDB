package com.test.db;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

class SimpleAdmin implements Admin {

	private final Properties prop;
	private final ConcurrentMap<String, Table> tables = new ConcurrentSkipListMap<>();

	SimpleAdmin(Properties prop) throws Exception {
		this.prop = prop;
		try {
			String res = this.prop.getProperty(ADMIN_IMPL);
			if(res != null){
				String[] ts = res.split(";");
				for(String s : ts) {
					String[] t = s.split("=");
					// create table
					createTable(t[0]);
					// create cells
					if (t.length != 1) {
						Table table = openTable(t[0]);
						String[] cells = t[1].split(",");
						for(int i = 0; i < cells.length; i = i + 9) {
							table.insert(Cell.createCell(BytesUtil.toBytes(cells[ i ]), Integer.parseInt(cells[i + 1]), Integer.parseInt(cells[i + 2]), 
														 BytesUtil.toBytes(cells[ i + 3 ]), Integer.parseInt(cells[i + 4]), Integer.parseInt(cells[i + 5]),
														 BytesUtil.toBytes(cells[ i + 6 ]), Integer.parseInt(cells[i + 7]), Integer.parseInt(cells[i + 8])));
						}
					}
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void createTable(String name) throws IOException {
		if (tables.containsKey(name)) {
			throw new IOException(name + " exists");
		}
		tables.computeIfAbsent(name, SimpleTable::new);
	}

	@Override
	public boolean tableExist(String name) throws IOException {
		return tables.containsKey(name);
	}

	@Override
	public void deleteTable(String name) throws IOException {
		if (tables.remove(name) == null) {
			throw new IOException(name + " not found");
		}
	}

	@Override
	public Table openTable(String name) throws IOException {
		Table t = tables.get(name);
		if (t == null) {
			throw new IOException(name + " not found");
		}
		return t;
	}

	@Override
	public List<String> listTables() throws IOException {
		return tables.keySet().stream().collect(Collectors.toList());
	}

	@Override
	public void close() throws IOException {
		// store database - outputStream to properties
		StringBuffer str = new StringBuffer();
		int i = 0;
		for(Entry<String, Table> entry : tables.entrySet()){
			str.append(entry.getKey() + "=" + entry.getValue().toString());
			if (++i < tables.size()) {
				str.append(";");
			}
		}
		prop.setProperty(ADMIN_IMPL, str.toString());
		FileOutputStream fos = new FileOutputStream(ADMIN_IMPL);
		prop.store(fos, null);
		fos.close();
	}

	private static class SimpleTable implements Table {

	    private static final CellComparator CELL_COMPARATOR = new CellComparator();
	    private final ConcurrentNavigableMap<Cell, Cell> data = new ConcurrentSkipListMap<>(CELL_COMPARATOR);
	    private final String name;

	    SimpleTable(final String name) {
	    	this.name = name;
	    }
	    
	    private boolean intToBool(int i){
	    	if(i == 0) {
	    		return true;
	    	}else{
	    		return false;
	    	}
	    }

	    @Override
	    public boolean insert(Cell cell) throws IOException {
	    	return data.put(cell, cell) != null;
	    }

	    @Override
	    public void delete(byte[] row) throws IOException {
	    	Cell rowOnlyCell = Cell.createRowOnly(row);
	    	for (Map.Entry<Cell, Cell> entry : data.tailMap(rowOnlyCell).entrySet()) {
	    		if (CellComparator.compareRow(entry.getKey(), rowOnlyCell) != 0) {
	    			return;
	    		} else {
	    			data.remove(entry.getKey());
	    		}
	    	}
	    }

	    @Override
	    public Iterator<Cell> get(byte[] row) throws IOException {
	    	Cell ele = Cell.createRowOnly(row);
			List<Cell> eles = data.keySet()
								  .stream()
								  .filter(cell -> intToBool(CellComparator.compareRow(ele, cell)))
								  .collect(Collectors.toList());
			return eles.iterator();
	    }

	    @Override
	    public Optional<Cell> get(byte[] row, byte[] column) throws IOException {
	    	return Optional.ofNullable(data.get(Cell.createRowColumnOnly(row, column)));
	    }

	    @Override
	    public boolean delete(byte[] row, byte[] column) throws IOException {
	    	return data.remove(Cell.createRowColumnOnly(row, column)) != null;
	    }

	    @Override
	    public boolean insertIfAbsent(Cell cell) throws IOException {
	    	return data.putIfAbsent(cell, cell) == null;
	    }

	    @Override
	    public void close() throws IOException {
	    	//nothing
	    }

	    @Override
	    public String getName() {
	    	return name;
	    }
	    
	    @Override
		public String toString() {
			StringBuffer str = new StringBuffer();
			int i = 0;
			for(Cell cell : data.keySet()){
				str.append(cell.toString());
				if(++i < data.size())
					str.append(",");
			}
			return str.toString();
		}
	}
}
