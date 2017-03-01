package com.cerner.jwala.service.balancermanager.impl.xml.data;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "manager", namespace = "http://httpd.apache.org")
public class Manager {

    private List<Balancer> balancers = new ArrayList<Balancer>();

    public List<Manager.Balancer> getBalancers() {
        return this.balancers;
    }

    @XmlElementWrapper(name = "balancers", namespace = "http://httpd.apache.org")
    @XmlElement(name = "balancer", namespace = "http://httpd.apache.org")
    public void setBalancers(final List<Manager.Balancer> balancers) {
        this.balancers = balancers;
    }

    @Override
    public String toString() {
        return "Manager{" +
                "balancers=" + balancers +
                '}';
    }

    @XmlRootElement
    public static class Balancer {
        private String name;
        private String stickysession;
        private String nofailover;
        private String timeout;
        private String lbmethod;
        private String scolonpathdelim;

        private List<Worker> workers = new ArrayList<Worker>();

        public List<Balancer.Worker> getWorkers() {
            return this.workers;
        }

        @XmlElementWrapper(name = "workers", namespace = "http://httpd.apache.org")
        @XmlElement(name = "worker", namespace = "http://httpd.apache.org")
        public void setWorkers(final List<Balancer.Worker> workers) {
            this.workers = workers;
        }

        public String getName() {
            return name;
        }

        @XmlElement(name = "name", namespace = "http://httpd.apache.org")
        public void setName(String name) {
            this.name = name;
        }

        public String getStickysession() {
            return stickysession;
        }

        @XmlElement(name = "stickysession", namespace = "http://httpd.apache.org")
        public void setStickysession(String stickysession) {
            this.stickysession = stickysession;
        }

        public String getNofailover() {
            return nofailover;
        }

        @XmlElement(name = "nofailover", namespace = "http://httpd.apache.org")
        public void setNofailover(String nofailover) {
            this.nofailover = nofailover;
        }

        public String getTimeout() {
            return timeout;
        }

        @XmlElement(name = "timeout", namespace = "http://httpd.apache.org")
        public void setTimeout(String timeout) {
            this.timeout = timeout;
        }

        public String getLbmethod() {
            return lbmethod;
        }

        @XmlElement(name = "lbmethod", namespace = "http://httpd.apache.org")
        public void setLbmethod(String lbmethod) {
            this.lbmethod = lbmethod;
        }

        public String getScolonpathdelim() {
            return scolonpathdelim;
        }

        @XmlElement(name = "scolonpathdelim", namespace = "http://httpd.apache.org")
        public void setScolonpathdelim(String scolonpathdelim) {
            this.scolonpathdelim = scolonpathdelim;
        }

        @Override
        public String toString() {
            return "Balancer{" +
                    "name='" + name + '\'' +
                    ", stickysession='" + stickysession + '\'' +
                    ", nofailover='" + nofailover + '\'' +
                    ", timeout='" + timeout + '\'' +
                    ", lbmethod='" + lbmethod + '\'' +
                    ", scolonpathdelim='" + scolonpathdelim + '\'' +
                    ", workers=" + workers +
                    '}';
        }

        @XmlRootElement
        public static class Worker {
            private String name;
            private String scheme;
            private String hostname;
            private int loadfactor;
            private int port;
            private int min;
            private int smax;
            private int max;
            private int ttl;
            private String keepalive;
            private String status;
            private int retries;
            private int lbstatus;
            private int transferred;
            private int read;
            private int elected;
            private String route;
            private String redirect;
            private int busy;
            private int lbset;
            private int retry;


            public String getName() {
                return name;
            }

            @XmlElement(name = "name", namespace = "http://httpd.apache.org")
            public void setName(String name) {
                this.name = name;
            }

            public String getScheme() {
                return scheme;
            }

            @XmlElement(name = "scheme", namespace = "http://httpd.apache.org")
            public void setScheme(String scheme) {
                this.scheme = scheme;
            }

            public String getHostname() {
                return hostname;
            }

            @XmlElement(name = "hostname", namespace = "http://httpd.apache.org")
            public void setHostname(String hostname) {
                this.hostname = hostname;
            }

            public int getLoadfactor() {
                return loadfactor;
            }

            @XmlElement(name = "loadfactor", namespace = "http://httpd.apache.org")
            public void setLoadfactor(int loadfactor) {
                this.loadfactor = loadfactor;
            }

            public int getPort() {
                return port;
            }

            @XmlElement(name = "port", namespace = "http://httpd.apache.org")
            public void setPort(int port) {
                this.port = port;
            }

            public int getMin() {
                return min;
            }

            @XmlElement
            public void setMin(int min) {
                this.min = min;
            }

            public int getSmax() {
                return smax;
            }

            @XmlElement(name = "smax", namespace = "http://httpd.apache.org")
            public void setSmax(int smax) {
                this.smax = smax;
            }

            public int getMax() {
                return max;
            }

            @XmlElement(name = "max", namespace = "http://httpd.apache.org")
            public void setMax(int max) {
                this.max = max;
            }

            public int getTtl() {
                return ttl;
            }

            @XmlElement(name = "ttl", namespace = "http://httpd.apache.org")
            public void setTtl(int ttl) {
                this.ttl = ttl;
            }

            public String getKeepalive() {
                return keepalive;
            }

            @XmlElement(name = "keepalive", namespace = "http://httpd.apache.org")
            public void setKeepalive(String keepalive) {
                this.keepalive = keepalive;
            }

            public String getStatus() {
                return status;
            }

            @XmlElement(name = "status", namespace = "http://httpd.apache.org")
            public void setStatus(String status) {
                this.status = status;
            }

            public int getRetries() {
                return retries;
            }

            @XmlElement(name = "retries", namespace = "http://httpd.apache.org")
            public void setRetries(int retries) {
                this.retries = retries;
            }

            public int getLbstatus() {
                return lbstatus;
            }

            @XmlElement(name = "lbstatus", namespace = "http://httpd.apache.org")
            public void setLbstatus(int lbstatus) {
                this.lbstatus = lbstatus;
            }

            public int getTransferred() {
                return transferred;
            }

            @XmlElement(name = "setTransferred", namespace = "http://httpd.apache.org")
            public void setTransferred(int transferred) {
                this.transferred = transferred;
            }

            public int getRead() {
                return read;
            }

            @XmlElement(name = "read", namespace = "http://httpd.apache.org")
            public void setRead(int read) {
                this.read = read;
            }

            public int getElected() {
                return elected;
            }

            @XmlElement(name = "elected", namespace = "http://httpd.apache.org")
            public void setElected(int elected) {
                this.elected = elected;
            }

            public String getRoute() {
                return route;
            }

            @XmlElement(name = "route", namespace = "http://httpd.apache.org")
            public void setRoute(String route) {
                this.route = route;
            }

            public String getRedirect() {
                return redirect;
            }

            @XmlElement(name = "redirect", namespace = "http://httpd.apache.org")
            public void setRedirect(String redirect) {
                this.redirect = redirect;
            }

            public int getBusy() {
                return busy;
            }

            @XmlElement(name = "busy", namespace = "http://httpd.apache.org")
            public void setBusy(int busy) {
                this.busy = busy;
            }

            public int getLbset() {
                return lbset;
            }

            @XmlElement(name = "lbset", namespace = "http://httpd.apache.org")
            public void setLbset(int lbset) {
                this.lbset = lbset;
            }

            public int getRetry() {
                return retry;
            }

            @XmlElement(name = "retry", namespace = "http://httpd.apache.org")
            public void setRetry(int retry) {
                this.retry = retry;
            }

            @Override
            public String toString() {
                return "Worker{" +
                        "name='" + name + '\'' +
                        ", scheme='" + scheme + '\'' +
                        ", hostname='" + hostname + '\'' +
                        ", loadfactor=" + loadfactor +
                        ", port=" + port +
                        ", min=" + min +
                        ", smax=" + smax +
                        ", max=" + max +
                        ", ttl=" + ttl +
                        ", keepalive='" + keepalive + '\'' +
                        ", status='" + status + '\'' +
                        ", retries=" + retries +
                        ", lbstatus=" + lbstatus +
                        ", transferred=" + transferred +
                        ", read=" + read +
                        ", elected=" + elected +
                        ", route='" + route + '\'' +
                        ", redirect='" + redirect + '\'' +
                        ", busy=" + busy +
                        ", lbset=" + lbset +
                        ", retry=" + retry +
                        '}';
            }
        }// End Worker
    }// End Balancer
}// End Manager
