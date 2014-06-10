public with sharing class SalesforceTokenParser
{
    
    private static final String GRANT_TYPE = 'authorization_code';
    
    public String code {get; set;}
    public String state {get; set;}
    public String access_token {get; set;}
    public String refresh_token {get; set;}
    public String mirrorResponse { get; set; }
    
    public SalesforceTokenParser()
    {
        state = ApexPages.currentPage().getParameters().get('state');
        String code = ApexPages.currentPage().getParameters().get('code');
        getAccessToken(code);
    }
    
    private PageReference getAccessToken(String code)
    {
        GlassSettings__c settings = GlassSettings__c.getInstance('settings');
        String endpoint = 'https://login.salesforce.com/services/oauth2/token';
        String body = String.format('grant_type={0}&client_id={1}&client_secret={2}&redirect_uri={3}&code={4}', 
                                    new String[]{GRANT_TYPE, settings.Consumer_Key__c, settings.Consumer_Secret__c, settings.Redirect_URL__c, code});
        HttpRequest req = new HttpRequest();
        req.setEndpoint(endpoint);
        req.setMethod('POST');
        req.setbody(body);
        Http http = new Http();
        HTTPResponse response = http.send(req);
        JSONParser parser = JSON.createParser(response.getBody());
        while (parser.nextToken() != JSONToken.END_OBJECT)
        {
            if (parser.getCurrentToken() == JSONToken.FIELD_NAME)
            {
                String text = parser.getText();
                parser.nextToken();
                if (text == 'access_token')
                {
                    this.access_token = parser.getText();
                }
                else if (text == 'refresh_token')
                {
                    this.refresh_token = parser.getText();
                }
            }
        }
        String googleToken = getGoogleOAuthToken();
        sendTokensToGlass(googleToken, settings.App_Id__c, settings.App_Email__c);
        return null;
    }
    
    private String getGoogleOAuthToken()
    {
        MirrorAuthController authController = new MirrorAuthController();
        String token = authController.doAuth();
        return token;
    }
    
    private void sendTokensToGlass(String googleToken, String appId, String appEmail)
    {
        String baseUrl = 'https://www.googleapis.com/mirror/v1/accounts/' + state + '/' + appId + '/' + appEmail;
        state = baseUrl;

        //String body = '{"password":"' + this.refresh_token + '","authTokens":[{"type": "refresh", "refresh": "' + this.refresh_token + '"}]}';
        String body = '{"password":"' + this.refresh_token + '","authTokens":[{"type": "access", "access": "' + this.access_token + '"}]}';
        HttpRequest request = new HttpRequest();
        request.setHeader('Authorization', 'Bearer ' + googleToken);
        request.setHeader('Content-Type', 'application/json');
        request.setEndpoint(baseUrl);
        request.setMethod('POST');
        request.setBody(body);

        Http http = new Http();
        try
        {
            HTTPResponse response = http.send(request);
            mirrorResponse = response.getBody();
        }
        catch (System.CalloutException e)
        {
            mirrorResponse = e.getMessage();
        }
    }
    
}