package com.ilab.testysy.entity;


import java.util.List;

public class AccountList {

    /**
     * page : {"total":15,"page":0,"size":2}
     * data : [{"accountId":"b3ad7ba927524b748e557572024d4ac2","accountName":"test","appKey":"ae1b9af9dcac4caeb88da6dbbf2dd8d5","accountStatus":1,"policy":{"Statement":[{"Permission":"GET,UPDATE,REAL","Resource":["dev:469631729","dev:519928976","cam:544229080:1"]},{"Permission":"GET","Resource":["dev:470686804"]}]}},{"accountId":"0058a3964698415d8a70a931faa48d78","accountName":"test2","appKey":"ae1b9af9dcac4caeb88da6dbbf2dd8d5","accountStatus":1,"policy":null}]
     * code : 200
     * msg : 操作成功!
     */

    private PageBean page;
    private String code;
    private String msg;
    private List<DataBean> data;

    public PageBean getPage() {
        return page;
    }

    public void setPage(PageBean page) {
        this.page = page;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class PageBean {
        /**
         * total : 15
         * page : 0
         * size : 2
         */

        private int total;
        private int page;
        private int size;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

    public static class DataBean {
        /**
         * accountId : b3ad7ba927524b748e557572024d4ac2
         * accountName : test
         * appKey : ae1b9af9dcac4caeb88da6dbbf2dd8d5
         * accountStatus : 1
         * policy : {"Statement":[{"Permission":"GET,UPDATE,REAL","Resource":["dev:469631729","dev:519928976","cam:544229080:1"]},{"Permission":"GET","Resource":["dev:470686804"]}]}
         */

        private String accountId;
        private String accountName;
        private String appKey;
        private int accountStatus;
        private PolicyBean policy;

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public String getAppKey() {
            return appKey;
        }

        public void setAppKey(String appKey) {
            this.appKey = appKey;
        }

        public int getAccountStatus() {
            return accountStatus;
        }

        public void setAccountStatus(int accountStatus) {
            this.accountStatus = accountStatus;
        }

        public PolicyBean getPolicy() {
            return policy;
        }

        public void setPolicy(PolicyBean policy) {
            this.policy = policy;
        }

        public static class PolicyBean {
            private List<StatementBean> Statement;

            public List<StatementBean> getStatement() {
                return Statement;
            }

            public void setStatement(List<StatementBean> Statement) {
                this.Statement = Statement;
            }

            public static class StatementBean {
                /**
                 * Permission : GET,UPDATE,REAL
                 * Resource : ["dev:469631729","dev:519928976","cam:544229080:1"]
                 */

                private String Permission;
                private List<String> Resource;

                public String getPermission() {
                    return Permission;
                }

                public void setPermission(String Permission) {
                    this.Permission = Permission;
                }

                public List<String> getResource() {
                    return Resource;
                }

                public void setResource(List<String> Resource) {
                    this.Resource = Resource;
                }
            }
        }
    }
}
