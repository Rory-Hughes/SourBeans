package ca.humanbeans.sourbeans.controller.api;

import ca.humanbeans.sourbeans.exception.ApiExceptionHandler;
import ca.humanbeans.sourbeans.exception.ApiRoutingExceptionHandler;
import ca.humanbeans.sourbeans.exception.ResourceConflictException;
import ca.humanbeans.sourbeans.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiExceptionHandlerTests.ProbeController.class)
@Import({ApiExceptionHandler.class, ApiRoutingExceptionHandler.class, ApiExceptionHandlerTests.ProbeController.class})
class ApiExceptionHandlerTests {
    @Autowired MockMvc mvc;

    @ParameterizedTest
    @CsvSource({"missing,404,Not Found", "conflict,409,Conflict", "unexpected,500,Internal Server Error"})
    void domainAndUnexpectedErrors(String kind, int status, String title) throws Exception {
        problem(mvc.perform(get("/api/_probe/" + kind).queryParam("secret", "do-not-return")),
                status, title, "/api/_probe/" + kind)
                .andExpect(content().string(not(containsString("do-not-return"))))
                .andExpect(content().string(not(containsString("password"))))
                .andExpect(content().string(not(containsString("java.lang"))))
                .andExpect(jsonPath("errors").doesNotExist());
    }

    @ParameterizedTest
    @CsvSource({"not-a-number", "0", "-1"})
    void invalidPathArguments(String id) throws Exception {
        problem(mvc.perform(get("/api/_probe/id/" + id)), 400, "Bad Request", "/api/_probe/id/" + id);
    }

    @Test
    void invalidBodyIncludesFieldMessagesWithoutRejectedValues() throws Exception {
        problem(mvc.perform(post("/api/_probe").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"SENSITIVE_TOO_LONG_VALUE\",\"state\":\"READY\"}")),
                400, "Bad Request", "/api/_probe")
                .andExpect(jsonPath("detail").value("Request validation failed."))
                .andExpect(jsonPath("errors[0].field").value("name"))
                .andExpect(jsonPath("errors[0].message").value("size must be between 3 and 10"))
                .andExpect(content().string(not(containsString("SENSITIVE_TOO_LONG_VALUE"))));
    }

    @ParameterizedTest
    @CsvSource({"'{broken'", "'{\"name\":\"valid\",\"state\":\"SECRET_INVALID_ENUM\"}'", "''"})
    void malformedAndMissingBodies(String body) throws Exception {
        problem(mvc.perform(post("/api/_probe").contentType(MediaType.APPLICATION_JSON).content(body)),
                400, "Bad Request", "/api/_probe")
                .andExpect(content().string(not(containsString("SECRET_INVALID_ENUM"))));
    }

    @Test
    void missingParameter() throws Exception {
        problem(mvc.perform(get("/api/_probe/query")), 400, "Bad Request", "/api/_probe/query");
    }

    @Test
    void wrongMethodPreservesAllow() throws Exception {
        problem(mvc.perform(put("/api/_probe")), 405, "Method Not Allowed", "/api/_probe")
                .andExpect(header().string("Allow", containsString("POST")));
    }

    @Test
    void wrongContentType() throws Exception {
        problem(mvc.perform(post("/api/_probe").contentType(MediaType.TEXT_PLAIN).content("secret")),
                415, "Unsupported Media Type", "/api/_probe");
    }

    @Test
    void unmappedApiRoute() throws Exception {
        problem(mvc.perform(get("/api/no-such-route")), 404, "Not Found", "/api/no-such-route");
    }

    @Test
    void nonApiRouteRetainsMvcFallback() throws Exception {
        mvc.perform(get("/no-such-page")).andExpect(status().isNotFound())
                .andExpect(content().string(not(containsString("about:blank"))));
    }

    @Test
    void validRequestIsUnaffected() throws Exception {
        mvc.perform(post("/api/_probe").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"valid\",\"state\":\"READY\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("name").value("valid"));
    }

    private ResultActions problem(ResultActions result, int status, String title, String path) throws Exception {
        return result.andExpect(status().is(status))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("type").value("about:blank"))
                .andExpect(jsonPath("title").value(title))
                .andExpect(jsonPath("status").value(status))
                .andExpect(jsonPath("detail", not(emptyString())))
                .andExpect(jsonPath("instance").value(path))
                .andExpect(jsonPath("trace").doesNotExist())
                .andExpect(jsonPath("exception").doesNotExist());
    }

    // Fixtures exist only in test sources; SET-03 adds no feature endpoints.
    @RestController
    @RequestMapping("/api/_probe")
    static class ProbeController {
        @PostMapping ProbeRequest create(@Valid @RequestBody ProbeRequest request) { return request; }
        @GetMapping("/id/{id}") long id(@PathVariable @Positive long id) { return id; }
        @GetMapping("/query") String query(@RequestParam String name) { return name; }
        @GetMapping("/missing") void missing() { throw new ResourceNotFoundException(); }
        @GetMapping("/conflict") void conflict() { throw new ResourceConflictException(); }
        @GetMapping("/unexpected") void unexpected() { throw new IllegalStateException("password=do-not-return"); }
    }

    enum State { READY }
    record ProbeRequest(@NotBlank @Size(min = 3, max = 10) String name, State state) { }
}
