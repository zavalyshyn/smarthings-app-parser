const fs = require('fs');

let capabilities = JSON.parse(fs.readFileSync('forum-capabilities.json', 'utf8'));
let attributes = JSON.parse(fs.readFileSync('forum-attributes-commands.json','utf8'));


let propercapabilities = [];

for (capability of capabilities) {
  let capDisplayName = capability.name;
  let capName = capability.smartAppStyleName;
  let propercap = null;
  for (attribute of attributes) {
    if (attribute[capDisplayName]) {
      // console.log("FOUND!!!")
      propercap = {
        "displayname" : capDisplayName,
        "name" : `capability.${capName}`,
        "attributes" : attribute[capDisplayName].attributes,
        "commands" : attribute[capDisplayName].commands
      }
      propercapabilities.push(propercap);
      break;
    }
  }
  if (propercap==null) {
    console.log("Couldn't find an attribute for capability: " + capability.name);
  }
}
let json = JSON.stringify(propercapabilities);

fs.writeFileSync('forum-capability-attributes-full.json', json, 'utf8');

// console.log(propercapabilities);
