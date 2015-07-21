package sample.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class IndexController {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@RequestMapping("/")
	@Transactional(readOnly = true)
	public ModelAndView index() {
		List<Note> notes = jdbcTemplate.query("SELECT * FROM Note", new RowMapper<Note>() {
			@Override
			public Note mapRow(ResultSet rs, int rowNum) throws SQLException {
				Note note = new Note();
				note.setBody(rs.getString("body"));
				note.setId(rs.getLong("id"));
				note.setTitle(rs.getString("title"));
				return note;
			}
		});
		ModelAndView modelAndView = new ModelAndView("index");
		modelAndView.addObject("notes", notes);
		return modelAndView;
	}

	public static class Note {
		private long id;
		private String body;
		private String title;

		public void setBody(String body) {
			this.body = body;
		}

		public String getBody() {
			return body;
		}

		public void setId(long id) {
			this.id = id;
		}

		public long getId() {
			return id;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
	}
}
