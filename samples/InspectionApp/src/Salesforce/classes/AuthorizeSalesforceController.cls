public class AuthorizeSalesforceController
{
    
    public AuthorizeSalesforceController()
    {
    }
    
    public PageReference doRedirect()
    {
		GlassSettings__c settings = GlassSettings__c.getInstance('settings');
        if (settings == null)
        {
            return new PageReference('/apex/Exception');
        }
        String consumerKey = settings.Consumer_Key__c;
        String userToken = ApexPages.currentPage().getParameters().get('userToken');
        String baseUrl = 'https://login.salesforce.com/services/oauth2/authorize?response_type=code&client_id=' + consumerKey;
        baseUrl += '&redirect_uri=' + settings.Redirect_URL__c;
        baseUrl += '&state=';
        String targetUrl = baseUrl + userToken;
        PageReference targetPage = new PageReference(targetUrl);
        targetPage.setRedirect(true);
        return targetPage;
    }
}