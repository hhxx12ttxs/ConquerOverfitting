/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * packages needed to communicate over http
 */
import java.net.*;
import java.io.*;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Chris Bougher
 *
 */
public class ServerConnector {

    //static members
    private static ServerConnector instance;
    //instance members
    private String domain;
    private String cookie;
    private boolean loggedIn;


    /* Private constructor */
    private ServerConnector(String domain) {
        this.domain = domain;
    }

    /**
     * Getters and Setters
     */
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * method to get the shared instance of this singleton
     * @param domain
     * @return ServerConnector Instance
     */
    public static ServerConnector sharedInstance(String domain) {
        if (instance == null) {
            instance = new ServerConnector(domain);
        }

        return instance;
    }

    /**
     * Attempts to log the user in and saves the returned
     * cookie if successful, throws an error otherwise
     * @param email
     * @param password
     * @throws ConnectionException
     * @throws LoginException
     */
    public void login(String email, String password) throws ConnectionException, LoginException {

        // don't try to login if already logged in
        if (loggedIn) {
            throw new LoginException("You are already logged in");
        }

        try {

            // Construct data for login, will result in a URL looking something like this
            // http://www.domain.com/java_login?name=admin&password=admin
            String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8");
            data += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8");

            // Send data
            URL url = new URL(domain + "/login/remote_login");
            URLConnection conn = url.openConnection();

            // this says use POST request method
            conn.setDoOutput(true);

            // now we can "write" the request data above as if writing a file
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                // this is the response data, could be an html page, could be xml or json
                if (!line.equals("")) {
                    throw new LoginException(line);
                }
            }

            // this loop will query the response headers that came from the server
            // as a result of the POST request. The only thing we need here is
            // the cookie value for the session id
            for (int i = 0;; i++) {
                String headerName = conn.getHeaderFieldKey(i);
                String headerValue = conn.getHeaderField(i);

                if (headerName == null && headerValue == null) {
                    // No more headers
                    break;
                }
                if ("Set-Cookie".equalsIgnoreCase(headerName)) {
                    // Parse cookie
                    String[] fields = headerValue.split(";\\s*");

                    String cookieValue = fields[0];

                    // Save the cookie...
                    cookie = cookieValue;
                }
            }
            wr.close();
            rd.close();

            // save logged in state
            loggedIn = true;

            // connection errors
        } catch (Exception e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    /**
     * Logout the user associated with the current cookie
     * @throws ConnectionException
     */
    public void logout() throws ConnectionException {

        // don't try to logout unless the cookie is set
        // indicating that the user is logged in
        if (!loggedIn) {
            return;
        }

        try {

            // Send data
            URL url = new URL(domain + "/login/remote_logout");
            URLConnection conn = url.openConnection();

            // Set the cookie value to send
            conn.setRequestProperty("Cookie", cookie);

            // Send the request to the server
            // assume logout is successful since there
            // is no error short of connection issues
            // or cookie issues
            // that would cause a failure
            conn.connect();

            // unset cookie and flag
            cookie = "";
            loggedIn = false;

            // connection errors
        } catch (Exception e) {
            throw new ConnectionException(e.getMessage());
        }
    }

    /**
     * return all sections for which the instructor is associated and where
     * the term is not expired
     *
     * @return
     * @throws ConnectionException
     * @throws LoginException
     * @throws ParseException
     *
     * example:
     * http://localhost:3000/instructor_portal.json
     * String s = "[{\"section\":{\"sequence\":1,\"created_at\":\"2010-03-22T22:42:43Z\",\"updated_at\":\"2010-03-22T22:42:43Z\",\"term_id\":1,\"id\":15,\"course_id\":2}}]";
     *
     */
    public Map[] getSections() throws ConnectionException, LoginException, ParseException {

        return getThings("/instructor_portal.json");
    }

    public Map[] getCourses() throws ConnectionException, LoginException, ParseException {

        return getThings("/instructor_portal/courses.json");
    }

    public Map[] getMeetingSchedules() throws ConnectionException, LoginException, ParseException {

        return getThings("/instructor_portal/meeting_schedules.json");
    }

    public Map[] getTerms() throws ConnectionException, LoginException, ParseException {

        return getThings("/instructor_portal/terms.json");
    }

    public Map[] getAssignments() throws ConnectionException, LoginException, ParseException {

        return getThings("/instructor_portal/assignments.json");
    }

    public Map[] getCategories() throws ConnectionException, LoginException, ParseException {

        return getThings("/instructor_portal/categories.json");
    }

    public Map[] getGradeletters() throws ConnectionException, LoginException, ParseException {

        return getThings("/instructor_portal/gradeletters.json");
    }

    public Map[] getStudents() throws ConnectionException, LoginException, ParseException {

        return getThings("/instructor_portal/students.json");
    }

    public Map[] getGrades() throws ConnectionException, LoginException, ParseException {

        return getThings("/instructor_portal/grades.json");
    }

    public Map[] getCampuses() throws ConnectionException, LoginException, ParseException {

        return getThings("/instructor_portal/campuses.json");
    }

    public Map[] getSectionStudents(long section_id) throws ConnectionException, LoginException, ParseException {

        return getThings("/sections/" + section_id + "/students.json");
    }

    public Map getInstructor() throws ConnectionException, LoginException, ParseException {

        return getThing("/instructor_portal/instructor.json");
    }

    public Map getSchool() throws ConnectionException, LoginException, ParseException {

        return getThing("/instructor_portal/school.json");
    }

    /**
     * Create a new grade letter
     * http://domain/sections/:section_id/gradeletters
     * post
     *
     * @param section_id
     * @param grade_letter
     * @param low_value
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void createGradeletter(long section_id, String grade_letter, int low_value) throws ConnectionException, LoginException, ValidationException {

        try {

            // Construct data for creating a grade, will result in a URL looking something like this
            //http://localhost:3000/sections/15/grade_assignment.json?assignment_id=4&student_id=9&points_earned=99
            String data = URLEncoder.encode("[gradeletter]grade_letter", "UTF-8") + "=" + URLEncoder.encode(grade_letter, "UTF-8");
            data += "&" + URLEncoder.encode("[gradeletter]low_value", "UTF-8") + "=" + low_value;

            // Send data
            String url_string = domain + "/sections/" + section_id + "/gradeletters.json";

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    /**
     * Update an existing grade letter
     * http://domain/sections/:section_id/gradeletters/:id
     * post _method=put
     *
     * @param id
     * @param section_id
     * @param grade_letter
     * @param low_value
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void updateGradeletter(long section_id, long id, String grade_letter, int low_value) throws ConnectionException, LoginException, ValidationException {

        try {

            // Construct data for creating a grade, will result in a URL looking something like this
            //http://localhost:3000/sections/15/grade_assignment.json?assignment_id=4&student_id=9&points_earned=99
            String data = URLEncoder.encode("[gradeletter]id", "UTF-8") + "=" + id;
            data += "&" + URLEncoder.encode("[gradeletter]grade_letter", "UTF-8") + "=" + URLEncoder.encode(grade_letter, "UTF-8");
            data += "&" + URLEncoder.encode("[gradeletter]low_value", "UTF-8") + "=" + low_value;
            data += "&" + URLEncoder.encode("_method", "UTF-8") + "=" + URLEncoder.encode("put", "UTF-8");

            // Send data
            String url_string = domain + "/sections/" + section_id + "/gradeletters/" + id + ".json";

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    /**
     * Destroy an existing grade letter
     * http://domain/sections/:section_id/gradeletters/:id
     * post _method=delete
     *
     * @param id
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void destroyGradeLetter(long section_id, long id) throws ConnectionException, LoginException, ValidationException {

        try {

            String data = URLEncoder.encode("_method", "UTF-8") + "=" + URLEncoder.encode("delete", "UTF-8");

            // Send data
            String url_string = domain + "/sections/" + section_id + "/gradeletters/" + id + ".json";

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    /**
     * Create a new category
     * http://domain/sections/:section_id/categories
     * post
     *
     * @param section_id
     * @param name
     * @param weight
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void createCategory(long section_id, String name, int weight) throws ConnectionException, LoginException, ValidationException {

        try {

            // Construct data for creating a grade, will result in a URL looking something like this
            //http://localhost:3000/sections/15/grade_assignment.json?assignment_id=4&student_id=9&points_earned=99
            String data = URLEncoder.encode("[category]name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
            data += "&" + URLEncoder.encode("[category]weight", "UTF-8") + "=" + weight;

            // Send data
            String url_string = domain + "/sections/" + section_id + "/categories.json";

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    /**
     * Update an existing Category
     * http://domain/sections/:section_id/categories/:id
     * post _method=put
     *
     * @param id
     * @param section_id
     * @param name
     * @param weight
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void updateCategory(long section_id, long id, String name, int weight) throws ConnectionException, LoginException, ValidationException {

        try {

            // Construct data for creating a grade, will result in a URL looking something like this
            //http://localhost:3000/sections/15/grade_assignment.json?assignment_id=4&student_id=9&points_earned=99
            String data = URLEncoder.encode("[category]id", "UTF-8") + "=" + id;
            data += "&" + URLEncoder.encode("[category]name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
            data += "&" + URLEncoder.encode("[category]weight", "UTF-8") + "=" + weight;
            data += "&" + URLEncoder.encode("_method", "UTF-8") + "=" + URLEncoder.encode("put", "UTF-8");

            // Send data
            String url_string = domain + "/sections/" + section_id + "/categories/" + id + ".json";

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    /**
     * Destroy an existing category
     * Update an existing Category
     * http://domain/sections/:section_id/categories/:id
     * post _method=delete
     *
     * @param id
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void destroyCategory(long section_id, long id) throws ConnectionException, LoginException, ValidationException {

        try {

            String data = URLEncoder.encode("_method", "UTF-8") + "=" + URLEncoder.encode("delete", "UTF-8");

            // Send data
            String url_string = domain + "/sections/" + section_id + "/categories/" + id + ".json";

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    /**
     * Create a new assignment
     *
     * @param section_id
     * @param category_id
     * @param name
     * @param strategy_id
     * @param points_value
     * @param points_curve
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void createAssignment(long section_id, long category_id, String name, long strategy_id, int points_value, int points_curve) throws ConnectionException, LoginException, ValidationException {

        try {

            String data = URLEncoder.encode("[assignment]name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
            data += "&" + URLEncoder.encode("[assignment]category_id", "UTF-8") + "=" + category_id;
            data += "&" + URLEncoder.encode("[assignment]strategy_id", "UTF-8") + "=" + strategy_id;
            data += "&" + URLEncoder.encode("[assignment]points_value", "UTF-8") + "=" + points_value;
            data += "&" + URLEncoder.encode("[assignment]points_curve", "UTF-8") + "=" + points_curve;

            // Send data
            String url_string = domain + "/sections/" + section_id + "/assignments.json";

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    /**
     * update an existing assignment
     *
     * @param id
     * @param section_id
     * @param category_id
     * @param name
     * @param strategy_id
     * @param points_value
     * @param points_curve
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void updateAssignment(long section_id, long id, long category_id, String name, long strategy_id, int points_value, int points_curve) throws ConnectionException, LoginException, ValidationException {

        try {

            // Construct data for creating a grade, will result in a URL looking something like this
            //http://localhost:3000/sections/15/grade_assignment.json?assignment_id=4&student_id=9&points_earned=99
            String data = URLEncoder.encode("[assignment]id", "UTF-8") + "=" + id;
            data += "&" + URLEncoder.encode("[assignment]name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");
            data += "&" + URLEncoder.encode("[assignment]category_id", "UTF-8") + "=" + category_id;
            data += "&" + URLEncoder.encode("[assignment]strategy_id", "UTF-8") + "=" + strategy_id;
            data += "&" + URLEncoder.encode("[assignment]points_value", "UTF-8") + "=" + points_value;
            data += "&" + URLEncoder.encode("[assignment]points_curve", "UTF-8") + "=" + points_curve;
            data += "&" + URLEncoder.encode("_method", "UTF-8") + "=" + URLEncoder.encode("put", "UTF-8");

            // Send data
            String url_string = domain + "/sections/" + section_id + "/assignments/" + id + ".json";

            //System.out.println(url_string);
            //System.out.println(category_id + " " + name + " " + strategy_id + " " + points_value + " " + points_curve);

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    /**
     * Destroy an existing assignment
     *
     * @param id
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void destroyAssignment(long section_id, long id) throws ConnectionException, LoginException, ValidationException {

        try {

            String data = URLEncoder.encode("_method", "UTF-8") + "=" + URLEncoder.encode("delete", "UTF-8");

            // Send data
            String url_string = domain + "/sections/" + section_id + "/assignments/" + id + ".json";

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    /**
     * Create new grade
     *
     * @param assignment_id
     * @param student_id
     * @param points_earned
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void createGrade(long section_id, long assignment_id, long student_id, int points_earned) throws ConnectionException, LoginException, ValidationException {

        try {

            // Construct data for creating a grade, will result in a URL looking something like this
            //http://localhost:3000/sections/15/grade_assignment.json?assignment_id=4&student_id=9&points_earned=99
            String data = URLEncoder.encode("assignment_id", "UTF-8") + "=" + assignment_id;
            data += "&" + URLEncoder.encode("student_id", "UTF-8") + "=" + student_id;
            data += "&" + URLEncoder.encode("points_earned", "UTF-8") + "=" + points_earned;

            // Send data
            String url_string = domain + "/sections/" + section_id + "/grade_assignment.json";

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }

    }

    /**
     * Update an existing grade
     *
     * @param id
     * @param assignment_id
     * @param student_id
     * @param points_earned
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void updateGrade(long section_id, long assignment_id, long student_id, int points_earned) throws ConnectionException, LoginException, ValidationException {

        //http://localhost:3000/sections/15/grade_assignment.json?assignment_id=4&student_id=9&points_earned=None
        try {

            // Construct data for creating a grade, will result in a URL looking something like this
            //http://localhost:3000/sections/15/grade_assignment.json?assignment_id=4&student_id=9&points_earned=99
            String data = URLEncoder.encode("assignment_id", "UTF-8") + "=" + assignment_id;
            data += "&" + URLEncoder.encode("student_id", "UTF-8") + "=" + student_id;
            data += "&" + URLEncoder.encode("points_earned", "UTF-8") + "=" + points_earned;

            // Send data
            String url_string = domain + "/sections/" + section_id + "/grade_assignment.json";

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }

    }

    /**
     * Destroy an existing grade
     *
     * @param id
     * @throws ConnectionException
     * @throws LoginException
     * @throws ValidationException
     */
    public void destroyGrade(long section_id, long assignment_id, long student_id) throws ConnectionException, LoginException, ValidationException {

        //http://localhost:3000/sections/15/grade_assignment.json?assignment_id=4&student_id=9&points_earned=None
        try {

            // Construct data for creating a grade, will result in a URL looking something like this
            //http://localhost:3000/sections/15/grade_assignment.json?assignment_id=4&student_id=9&points_earned=99
            String data = URLEncoder.encode("assignment_id", "UTF-8") + "=" + assignment_id;
            data += "&" + URLEncoder.encode("student_id", "UTF-8") + "=" + student_id;
            data += "&" + URLEncoder.encode("points_earned", "UTF-8") + "=" + URLEncoder.encode("None");

            // Send data
            String url_string = domain + "/sections/" + section_id + "/grade_assignment.json";

            // do the post request
            makePostRequest(url_string, data);

        } catch (UnsupportedEncodingException ex) {
            throw new ConnectionException(ex.getMessage());
        }
    }

    /**
     * converts a JSON string into a Java map
     *
     * @param toParse
     * @return
     * @throws ParseException
     */
    private Map[] mapArrayFromJSONString(String toParse) throws ParseException {

        JSONParser parser = new JSONParser();
        Map[] objects;

        // parse JSON string into an array
        Object obj = parser.parse(toParse);
        JSONArray array = (JSONArray) obj;

        objects = new Map[array.size()];

        // add each entry of the array to the map
        for (int x = 0; x < array.size(); x++) {

            // get the map for this entry
            JSONObject obj2 = (JSONObject) array.get(x);

            // get the key for ths item type "section", "course", etc
            Object key = obj2.keySet().toArray()[0];
            JSONObject obj3 = (JSONObject) obj2.get(key);

            // get the map for the specific item type {"name":"Rover", "type":"Dog"}
            objects[x] = obj3;

        }

        return objects;
    }

    /**
     * converts a JSON string into a Java map
     *
     * @param toParse
     * @return
     * @throws ParseException
     */
    private Map mapFromJSONString(String toParse) throws ParseException {

        JSONParser parser = new JSONParser();

        // parse JSON string into a map
        Object obj = parser.parse(toParse);
        JSONObject obj1 = (JSONObject) obj;

        // get the key for ths item type "section", "course", etc
        Object key = obj1.keySet().toArray()[0];
        JSONObject obj3 = (JSONObject) obj1.get(key);

        return (Map) obj3;
    }

    /**
     * returns all things retrieved from server based on partial url passed in
     *
     * @param things
     * @return
     * @throws ConnectionException
     * @throws LoginException
     * @throws ParseException
     */
    private Map[] getThings(String things) throws ConnectionException, LoginException, ParseException {

        String response;

        // can only do this if logged in
        if (loggedIn) {

            try {
                // set request url
                String URLString = domain + things;
                URL url = new URL(URLString);
                URLConnection conn = url.openConnection();

                // Set the cookie value to send
                conn.setRequestProperty("Cookie", cookie);

                // Send the request to the server
                conn.connect();

                // Get the response, this should be JSON
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                response = rd.readLine();

            } catch (MalformedURLException e) {
                throw new ConnectionException(e.getMessage());
            } catch (IOException e) {
                throw new ConnectionException(e.getMessage());
            }

            // convert the JSON to a map array
            return mapArrayFromJSONString(response);

        } else {
            throw new LoginException("You must log in to perform this action");
        }
    }

    /**
     * returns a single thing from the server based on the partial url passed in
     *
     * @param things
     * @return
     * @throws ConnectionException
     * @throws LoginException
     * @throws ParseException
     */
    private Map getThing(String things) throws ConnectionException, LoginException, ParseException {

        String response;

        // can only do this if logged in
        if (loggedIn) {

            try {
                // set request url
                String URLString = domain + things;
                URL url = new URL(URLString);
                URLConnection conn = url.openConnection();

                // Set the cookie value to send
                conn.setRequestProperty("Cookie", cookie);

                // Send the request to the server
                conn.connect();

                // Get the response, this should be JSON
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                response = rd.readLine();

            } catch (MalformedURLException e) {
                throw new ConnectionException(e.getMessage());
            } catch (IOException e) {
                throw new ConnectionException(e.getMessage());
            }

            // convert the JSON to a map array
            return mapFromJSONString(response);

        } else {
            throw new LoginException("You must log in to perform this action");
        }
    }

    private void makePostRequest(String url_string, String data) throws ConnectionException, LoginException, ValidationException {

        // don't try to login if already logged in
        if (loggedIn) {

            try {

                // Send data
                URL url = new URL(url_string);
                URLConnection conn = url.openConnection();

                // Set the cookie value to send
                conn.setRequestProperty("Cookie", cookie);

                // this says use POST request method
                conn.setDoOutput(true);

                // now we can "write" the request data above as if writing a file
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    // this is the response data, could be an html page, could be xml or json
                    if (!line.equals(" ")) {
                        //System.out.println(line);
                        throw new ValidationException(line);
                    }
                }

                wr.close();
                rd.close();

                // connection errors
            } catch (Exception e) {
                throw new ConnectionException(e.getMessage());
            }
        } else {
            throw new LoginException("You must log in to perform this action");
        }
    }
}

