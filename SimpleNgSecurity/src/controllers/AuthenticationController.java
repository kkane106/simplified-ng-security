package controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import data.UserDao;
import entities.User;
import models.JsonWebTokenGenerator;
import security.SecretKeyGenerator;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
  @Autowired
  JsonWebTokenGenerator jwtGen;

  @Autowired
  UserDao userDao;
  
  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public Map<String,String> login(HttpServletRequest req, HttpServletResponse res, @RequestBody String userJsonString) {
    ObjectMapper mapper = new ObjectMapper();
    User user = null;
    try {
      user = mapper.readValue(userJsonString, User.class);
    } catch (Exception e) {
      e.printStackTrace();
      res.setStatus(422);
      responseJson.put("error", "Client data was malformed and unparseable");
      return responseJson;
    }
    try {
      user = userDao.authenticateUser(user);
    } catch (NoResultException e) {
      Map<String,String> errJson = new HashMap<>();
      errJson.put("message", "Username not found");
      return new ResponseEntity<Map<String,String>>(errJson, HttpStatus.NOT_FOUND);
    }
    if (user != null) {
      String jws = jwtGen.generateUserJwt(user.getId());
      Map<String,String> success = new HashMap<>();
      success.put("jwt", jws);
      return new ResponseEntity<Map<String,String>>(success, HttpStatus.OK);
    }
    return new ResponseEntity<String>("Incorrect password", HttpStatus.UNAUTHORIZED);
  }

  @RequestMapping(value = "/signup", method = RequestMethod.POST)
  public Map<String,String> signup(HttpServletRequest req, HttpServletResponse res, @RequestBody String userJson) {
    Map<String,String> responseJson = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    try {
      User user = mapper.readValue(userJson, User.class);
    } catch (IOException ie) {
      ie.printStackTrace();
      res.setStatus(422);
      responseJson.put("error", "Client data was malformed and unparseable");
      return responseJson;
    }
    user = userDao.create(user);
    if (user != null) {
      String jws = jwtGen.generateUserJwt(user);
      responseJson.put("jwt", jws);
      return responseJson;
    }
    res.setStatus(500);
    responseJson.put("error", "Internal Server Error");
    return responseJson;
  }
}
