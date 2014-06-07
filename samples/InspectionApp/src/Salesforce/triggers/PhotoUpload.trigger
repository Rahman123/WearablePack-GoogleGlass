trigger PhotoUpload on Attachment (after insert, after update) {
    
    List<Inspection_Step__c> steps = new List<Inspection_Step__c>();
    for (Attachment a : Trigger.new){
        if (a.ParentId.getSObjectType().getDescribe().getName() == 'Inspection_Step__c' &&
            (a.ContentType == 'image/png' || a.ContentType == 'image/jpeg' || a.ContentType == 'image/gif')){
			Inspection_Step__c s = new Inspection_Step__c(Id = a.ParentId,
                                                          Photo__c = a.Id); 
            steps.add(s);    
        }
    }
    update steps;
}