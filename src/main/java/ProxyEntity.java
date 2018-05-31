public class ProxyEntity {
    boolean success;
    String msg;
    data data;
    class data{
        String ip;
        int port;

        @Override
        public String toString() {
            return "data{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    '}';
        }
    }

    public ProxyEntity() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public ProxyEntity.data getData() {
        return data;
    }

    public void setData(ProxyEntity.data data) {
        this.data = data;
    }
    public String getIp() {
        return data.ip;
    }
    public int getPort(){
        return data.port;
    }

    @Override
    public String toString() {
        return "ProxyEntity{" +
                "success=" + success +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
