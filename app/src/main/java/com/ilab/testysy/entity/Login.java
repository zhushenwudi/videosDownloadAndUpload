package com.ilab.testysy.entity;

public class Login {

    /**
     * code : 0
     * data : {"token":"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbmlzdHJhdG9yIiwic2NvcGVzIjpbXSwidXNlcl9pZCI6MSwiaWF0IjoxNTY0MDQ3MTY1LCJleHAiOjE3MjE3MjcxNjV9.IzjrNzF9Ln_MTGG2g4HwQvOlY6YA4oQX4fvs1aZVYZ2ykQSH4AYZACIqg4cSPz8FvRlQU2N1oacg25IZo4MvGg","refreshToken":"eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbmlzdHJhdG9yIiwic2NvcGVzIjpbIlJPTEVfUkVGUkVTSF9UT0tFTiJdLCJqdGkiOiIyZTA4YWFhOC00MzQ3LTRjNjUtYmI4Ni0zOGQzMjI3YjgzM2IiLCJpYXQiOjE1NjQwNDcxNjUsImV4cCI6MTU2NDA4MzE2NX0.SrZQk4oxansQ510UNLWEyRUTOCuFy7n4QmPsdwDJhKJdgiPUefkrpQmPIpvsTe51D9sG7KbWdXKdO2goV54H3A"}
     * message : Succeed
     */

    private int code;
    private DataBean data;
    private String message;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class DataBean {
        /**
         * token : eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbmlzdHJhdG9yIiwic2NvcGVzIjpbXSwidXNlcl9pZCI6MSwiaWF0IjoxNTY0MDQ3MTY1LCJleHAiOjE3MjE3MjcxNjV9.IzjrNzF9Ln_MTGG2g4HwQvOlY6YA4oQX4fvs1aZVYZ2ykQSH4AYZACIqg4cSPz8FvRlQU2N1oacg25IZo4MvGg
         * refreshToken : eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbmlzdHJhdG9yIiwic2NvcGVzIjpbIlJPTEVfUkVGUkVTSF9UT0tFTiJdLCJqdGkiOiIyZTA4YWFhOC00MzQ3LTRjNjUtYmI4Ni0zOGQzMjI3YjgzM2IiLCJpYXQiOjE1NjQwNDcxNjUsImV4cCI6MTU2NDA4MzE2NX0.SrZQk4oxansQ510UNLWEyRUTOCuFy7n4QmPsdwDJhKJdgiPUefkrpQmPIpvsTe51D9sG7KbWdXKdO2goV54H3A
         */

        private String token;
        private String refreshToken;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }
    }
}
