package com.zdc.luence.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.zdc.luence.model.Book;

@Service
public class DataSerchService {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public List<Book> search() {
		List<Book> bookList = this.jdbcTemplate.query("select * from book", new RowMapper<Book>() {

			public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
				Book book = new Book();
				book.setId(rs.getInt("id"));
				book.setName(rs.getString("name"));
				book.setPrice(rs.getFloat("price"));
				book.setDescription(rs.getString("description"));
				book.setPic(rs.getString("pic"));
				return book;
			}
		});
		return bookList;
	}

	public void insetData() {
		for(int i = 0; i < 10000; i++) {
			this.jdbcTemplate.execute("insert into book (name,price,description,pic) VALUES ('java" + UUID.randomUUID()
					+ "', '112" + i + "', 'javaSE事宜阿什顿发货施蒂利克发号施令款到发货施蒂利克', '123123.jpg')");
		}

	}

	public static void main(String[] args) {
		System.out.println(UUID.randomUUID());

	}
}
