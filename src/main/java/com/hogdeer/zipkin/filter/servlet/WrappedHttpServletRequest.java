package com.hogdeer.zipkin.filter.servlet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Administrator
 */
public class WrappedHttpServletRequest extends HttpServletRequestWrapper {

    private static final Logger logger = LoggerFactory.getLogger(WrappedHttpServletRequest.class);
    private byte[] bytes;
    private WrappedHttpServletRequest.WrappedServletInputStream wrappedServletInputStream;

    public WrappedHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);

        try {
            this.bytes = this.reaplBytes(request);
            if (null != this.bytes) {
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.bytes);
                this.wrappedServletInputStream = new WrappedHttpServletRequest.WrappedServletInputStream(byteArrayInputStream);
                this.reWriteInputStream();
            }
        } catch (Exception var3) {
            logger.error(var3.getMessage(), var3);
        }

    }

    public void reWriteInputStream() {
        this.wrappedServletInputStream.setStream(new ByteArrayInputStream(this.bytes != null ? this.bytes : new byte[0]));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.wrappedServletInputStream;
    }

    public String getRequestParams() throws IOException {
        String param = "";

        try {
            if (null != this.bytes) {
                param = new String(this.bytes, this.getCharacterEncoding());
            }
        } catch (Exception var3) {
            logger.error(var3.getMessage(), var3);
        }

        return param;
    }

    private byte[] reaplBytes(HttpServletRequest request) {
        try {
            String param = IOUtils.toString(request.getInputStream(), this.getCharacterEncoding());
            if (StringUtils.isNotBlank(param)) {
                String result = "\"\"";
                Pattern pattern = Pattern.compile(result);
                Matcher mat = pattern.matcher(param);
                param = mat.replaceAll("null");
            }

            return param.getBytes(this.getCharacterEncoding());
        } catch (Exception var6) {
            logger.error(var6.getMessage(), var6);
            return null;
        }
    }

    private class WrappedServletInputStream extends ServletInputStream {
        private InputStream stream;

        public void setStream(InputStream stream) {
            this.stream = stream;
        }

        public WrappedServletInputStream(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public int read() throws IOException {
            return this.stream.read();
        }

        public boolean isFinished() {
            return true;
        }

        public boolean isReady() {
            return true;
        }

        //public void setReadListener(ReadListener readListener) {
        //}
    }

}
