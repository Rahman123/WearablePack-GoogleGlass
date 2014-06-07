public class MirrorAuthController
{
    public String jwt {get; set;}
    public String jwtHeader {get; set;}
    public String jwtClaimSet {get; set;}
    public String jwtClaimSetJSON {get; set;}
    public String jwtSignature {get; set;}
    public String token { get; set; }
        
    public String doAuth()
    {
        // Header is a constant
        jwtHeader = '{"alg":"RS256","typ":"JWT"}';
        jwtClaimSet = createClaimSet();
        SignServer signServer = new SignServer('http://whispering-dusk-6117.herokuapp.com/sign');
        jwtSignature = signServer.sign(encoded(jwtHeader) + '.' + encoded(jwtClaimSet));
        jwt = String.format('{0}.{1}', new List<String>{encoded(jwtHeader) + '.' + encoded(jwtClaimSet), jwtSignature});
        
   		HttpRequest req = new HttpRequest();
		String content = 'grant_type=' + EncodingUtil.urlEncode('urn:ietf:params:oauth:grant-type:jwt-bearer', 'utf-8') +
			'&assertion=' + EncodingUtil.urlEncode(jwt, 'utf-8');
		req.setEndpoint('https://accounts.google.com/o/oauth2/token');
		req.setHeader('Content-Type','application/x-www-form-urlencoded');
		req.setMethod('POST');
		req.setBody(content);
        
        Http http = new Http();
		HttpResponse res = http.send(req);
        GoogleOAuthResponse response = (GoogleOAuthResponse)JSON.deserialize(res.getBody(), GoogleOAuthResponse.class);
        token = response.access_token;
        return token;
    }
    
    public String createClaimSet()
    {
        Datetime issueTime = Datetime.now();
        Datetime expiryTime = issueTime.addMinutes(60);

        ClaimSet claimSet = new ClaimSet();
        claimSet.iss = '781446821827-1q5bh0rur6hei7b6uc4gm4s4r57qit08@developer.gserviceaccount.com';
        claimSet.scope = 'https://www.googleapis.com/auth/glass.thirdpartyauth';
        claimSet.aud = 'https://accounts.google.com/o/oauth2/token';
        claimSet.iat = issueTime.getTime() / 1000;
        claimSet.exp = expiryTime.getTime() / 1000;

        String claimString = claimSet.toString();
        return claimString;
    }
    
    public String encoded(String input)
    {
        Blob claimBlob = Blob.valueOf(input);
        String encodedClaim = base64(claimBlob);
        return encodedClaim;
    }
    
    private static String base64(Blob b) {
		String ret = EncodingUtil.base64Encode(b);
		ret = ret.replaceAll('\\+', '-');
		ret = ret.replaceAll('/', '_');
		ret = ret.replaceAll('=', '');
		return ret;
	}

	private class ClaimSet 
	{
    	public String iss {get; set;}
	    public String scope {get; set;}
    	public String aud {get; set;}
	    public Long exp {get; set;}
    	public Long iat {get; set;}
    
    	public override String toString() 
        {
			String format = '"iss":"{0}","scope":"{1}","aud":"{2}","exp": {3},"iat": {4}';
			List<String> input = new List<String>();
			input.add(iss);
			input.add(scope);
			input.add(aud);
			input.add(String.valueOf(exp));
			input.add(String.valueOf(iat));
			return '{' + String.format(format, input) + '}';
		}
	}
    
    private class GoogleOAuthResponse
    {
        public String access_token { get; set; }
        public String token_type { get; set; }
        public String expires_in { get; set; }
    }
}