package com.ethanace.royalereport;

/**
 * @author ethanace
 */

public class ReportModel {
    
    private NetModel netModel = new NetModel();
    
    private String buildReport(String tag, String auth) {
        
        return null;
        
    }

    public boolean buildPerformanceReport(String tag, String token) {

        String[] columnHeaders = {"Tag", "Name", "Participation Rate"};
        String template = "https://api.clashroyale.com/v1/clans/%%23%s/riverracelog";
        String url = String.format(template, tag);
        String response = netModel.HTTPGet(url, token);
        
        System.out.println(response);
        
        return false;
    }
    
}
