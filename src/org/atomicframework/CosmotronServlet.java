package org.atomicframework;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.AutoIndentWriter;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.ContentExchange;

public class CosmotronServlet extends HttpServlet {
    public static final long serialVersionUID = 1;

    private ServletConfig config;
    private HttpClient client;
    private STGroup group;

    // Public API Methods ------------------------------------------------------

    public void init (ServletConfig config) throws ServletException {
	try {
	    this.loadTemplates();
	    this.config = config;
	    this.client = new HttpClient();
	    this.client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
	    this.client.start();}
	catch (Exception e) {throw new ServletException(e);}}

    public void doGet (HttpServletRequest request, HttpServletResponse response) 
	throws IOException, ServletException {
	try {
	    this.loadTemplates();
	    this.generateResponse(request, response);}
	catch (Exception e) {throw new ServletException(e);}}
    
    // Private Helper Methods --------------------------------------------------

    private void loadTemplates () {
	this.group = new STGroupFile("cosmotron.stg");}

    private void generateResponse (HttpServletRequest request, HttpServletResponse response) 
	throws Exception {
	response.setContentType("application/xml");
	ContentExchange contentExchange = new ContentExchange();
	contentExchange.setURL("http://localhost:8080/atomic" + request.getPathInfo());
	this.client.send(contentExchange);
	contentExchange.waitForDone();
	ST t = this.group.getInstanceOf("PREAMBLE");
	t.add("xsl", "cosmotron.xsl");
	t.write(new AutoIndentWriter(response.getWriter()));
	response.getWriter().write(contentExchange.getResponseContent());
	response.setStatus(HttpServletResponse.SC_OK);}

    private static void copyStream (InputStream input, OutputStream output) throws IOException {
	byte[] buffer = new byte[1024];
	int bytesRead;
	while ((bytesRead = input.read(buffer))!=-1) output.write(buffer, 0, bytesRead);}}
