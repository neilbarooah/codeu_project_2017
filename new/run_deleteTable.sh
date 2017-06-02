PROJECTID="codeuproject"
INSTANCEID="codeuinstance"
USERTABLE="user"
CONVERSATIONTABLE="conversation"
GROUPTABLE="group"
MESSAGETABLE="message"

mvn exec:java@delete \
    "-DprojectId=$PROJECTID" \
    "-DinstanceId=$INSTANCEID" \
    "-DuserTable=$USERTABLE" \
    "-DconversationTable=$CONVERSATIONTABLE" \
    "-DgroupTable=$GROUPTABLE" \
    "-DmessageTable=$MESSAGETABLE"