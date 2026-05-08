package com.energytrading.dto;

import java.util.List;

public class TracePathDTO {
    private String txHash;
    private List<TraceNode> nodes;
    private String summary;

    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public List<TraceNode> getNodes() { return nodes; }
    public void setNodes(List<TraceNode> nodes) { this.nodes = nodes; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public static class TraceNode {
        private Integer step;
        private String type;
        private String title;
        private String description;
        private String address;
        private String userName;
        private String timestamp;
        private String status;

        public Integer getStep() { return step; }
        public void setStep(Integer step) { this.step = step; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
