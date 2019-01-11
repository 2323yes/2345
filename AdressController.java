package cz.jh.sos.controller;

import cz.jh.sos.model.Adress;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

;

@RestController
public class AdressController {

    static final int PAGE_SIZE = 3;

    JdbcTemplate jdbcTemplate;

    public AdressController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/test")
    public String test() {
        return "Hello world!";
    }

    @GetMapping("/adress/{id}")
    public Adress getAdress(@PathVariable Long id) {
        return jdbcTemplate.queryForObject(
                "SELECT id, street, city, zip FROM adress WHERE id = ?",
                new Object[]{id},
                new BeanPropertyRowMapper<>(Adress.class));
    }

    @GetMapping("/adress")
    public List<Adress> getAdresses(
            @RequestParam(required = false, defaultValue = "1") Integer pageNo) {
        return jdbcTemplate.query(
                "SELECT id, street, city, zip FROM adress ORDER BY id LIMIT ?,?",
                new Object[]{getHowMuchRowsToSkip(pageNo), PAGE_SIZE},
                new BeanPropertyRowMapper<>(Adress.class));
    }

    @PostMapping("/adress")
    public Adress createAdress(@RequestBody Adress adress) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO adress (street, city, zip) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, adress.getStreet());
            preparedStatement.setString(2, adress.getCity());
            preparedStatement.setString(3, adress.getZip());

            return preparedStatement;
        }, keyHolder);

        long newAdressId = keyHolder.getKey().longValue();
        return getAdress(newAdressId);
    }

    @PutMapping("/adress/{id}")
    public Adress updateAdress(@PathVariable Long id, @RequestBody Adress adress) {
        jdbcTemplate.update("UPDATE adress SET street = ?, city = ?, zip = ? WHERE id = ?",
                adress.getStreet(), adress.getCity(), adress.getZip(), id);

        return getAdress(id);
    }

    @DeleteMapping("adress/{id}")
    public ResponseEntity deleteAdress(@PathVariable Long id) {
        jdbcTemplate.update("DELETE FROM adress WHERE id = ?", id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private int getHowMuchRowsToSkip(int pageNo) {
        if (pageNo <= 0) {
            throw new IllegalArgumentException("invalid number of page");
        }

        return (pageNo - 1) * PAGE_SIZE;
    }

}
