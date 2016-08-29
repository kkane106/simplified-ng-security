package controllers;

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
    }
    try {
      user = userDao.authenticateUser(user);
    } catch (Exception e) {
      e.printStackTrace();
    }
    String jws = jwtGen.generateUserJwt(user);
    Map<String, String> responseJson = new HashMap<>();
    responseJson.put("jwt", jws);
    return responseJson;
  }

  @RequestMapping(value = "/signup", method = RequestMethod.POST)
  public Map<String,String> signup(HttpServletRequest req, HttpServletResponse res, @RequestBody String userJson) {
    ObjectMapper mapper = new ObjectMapper();
    User user = null;
    try {
      user = mapper.readValue(userJson, User.class);
    } catch (IOException ie) {
      ie.printStackTrace();
    }
    user = userDao.create(user);
    String jws = jwtGen.generateUserJwt(user);
    Map<String,String> responseJson = new HashMap<>();
    responseJson.put("jwt", jws);
    return responseJson;
  }
}
