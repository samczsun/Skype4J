/*
 * Copyright 2015 Sam Sun <me@samczsun.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.samczsun.skype4j.formatting.lang.en;

import com.samczsun.skype4j.formatting.IEmoticon;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Emoticon implements IEmoticon {

    MONKEY("monkey","137eeecb-fe07-432c-889d-b0e3e32334af","Monkey","(monkey)","(Monkey)","(ape)","(Ape)",":(|)","(monkey)","(Monkey)","(ape)","(Ape)",":(|)"),
    SMILE("smile","f409d940-3efd-4853-95a4-e5f840835a83","Smile",":)",":-)",":=)","(smile)","(Smile)",":)",":-)",":=)","(smile)","(Smile)"),
    SAD("sad","e34deb20-f8f5-4b53-ab00-468ccacfac69","Sad",":(",":-(",":=(","(sad)","(Sad)",":<",":-<",":(",":-(",":=(","(sad)","(Sad)",":<",":-<"),
    LAUGH("laugh","516b20f2-a771-4840-95e6-1b75171eb440","Laugh",":D",":-D",":=D",":d",":-d",":=d","(laugh)","(Laugh)",":>",":->","(lol)","(LOL)","(Lol)",":D",":-D",":=D",":d",":-d",":=d","(laugh)","(Laugh)",":>",":->","(lol)","(LOL)","(Lol)"),
    CWL("cwl","e5385d74-9bb0-4065-bc44-c05bc48e1e6e","Crying with laughter","(cwl)","(Cwl)","(cryingwithlaughter)","(Cryingwithlaughter)"),
    COOL("cool","9c61c56e-7914-49a5-8b07-7ee63086c8b7","Cool","(cool)","(Cool)","8-)","8=)","B-)","B=)","(cool)","(Cool)","8-)","8=)","B-)","B=)"),
    SURPRISED("surprised","f0e607d2-6c29-48d7-9790-db6bafe45d5c","Surprised",":O",":-O",":=O",":o",":-o",":=o","(surprised)","(Surprised)",":O",":-O",":=O",":o",":-o",":=o","(surprised)","(Surprised)"),
    WINK("wink","e7b6dac3-8611-49c6-a028-620686cea594","Wink",";)",";-)",";=)","(wink)","(Wink)",";)",";-)",";=)","(wink)","(Wink)"),
    CRY("cry","9137cb01-0ab6-4374-9c34-4bd7ad4ceaad","Crying",";(",";-(",";=(","(cry)","(Cry)",":'(",";(",";-(",";=(","(cry)","(Cry)",":'("),
    SWEAT("sweat","51c1de7f-d0aa-4d57-a3b1-1baf840b90f5","Sweating","(:|","(sweat)","(Sweat)","(:|","(sweat)","(Sweat)"),
    SPEECHLESS("speechless","8a568d2c-32f0-44ca-a924-5781781cbf22","Speechless",":|",":-|",":=|","(speechless)","(Speechless)",":|",":-|",":=|","(speechless)","(Speechless)"),
    KISS("kiss","5b17b956-c425-415a-8224-4caf989128c8","Kiss","(kiss)","(Kiss)",":*",":-*",":=*","(xo)","(K)","(k)","(kiss)","(Kiss)",":*",":-*",":=*","(xo)","(K)","(k)"),
    TONGUEOUT("tongueout","d6f3c046-1e7e-4419-9a7e-f397fb2331a9","Cheeky",":P",":-P",":=P",":p",":-p",":=p","(tongueout)","(Tongueout)",":P",":-P",":=P",":p",":-p",":=p","(tongueout)","(Tongueout)"),
    BLUSH("blush","fc46c8b8-9002-4948-b671-27d6e0e6dec4","Blushing",":$",":-$",":=$",":\">","(blush)","(Blush)",":$",":-$",":=$",":\">","(blush)","(Blush)"),
    WONDER("wonder","5b61e42a-2d2e-4131-ace1-06e371f8c4d5","Wondering",":^)","(wonder)","(Wonder)",":^)","(wonder)","(Wonder)"),
    SLEEPY("sleepy","66358b4d-1d98-4d81-9965-f82e75a938c7","Sleepy","|-)","I-)","I=)","(snooze)","(Snooze)","|-)","I-)","I=)","(snooze)","(Snooze)","(sleepy)","(Sleepy)"),
    DULL("dull","8b43b914-c273-462d-9ca0-9d6c894c7be0","Dull","|-(","|(","|=(","(dull)","(Dull)","|-(","|(","|=(","(dull)","(Dull)"),
    INLOVE("inlove","8da36590-765e-4494-a400-a8b92b5f4b51","In love","(inlove)","(Inlove)","(love)","(Love)",":]",":-]","(inlove)","(Inlove)","(love)","(Love)",":]",":-]"),
    EG("eg","ee33852b-750c-428f-8901-daecd7fa11f6","Evil grin","]:)",">:)","(grin)","(Grin)","]:)",">:)","(evilgrin)","(Evilgrin)","(evil)","(Evil)","(grin)","(Grin)","(eg)","(Eg)"),
    YAWN("yawn","58f34a45-86a8-4b66-9a36-664020570614","Yawn","(yawn)","(Yawn)","(yawn)","(Yawn)"),
    PUKE("puke","4189f621-f0d3-4bc3-a560-e53b3174c05c","Vomiting","(puke)","(Puke)",":&",":-&",":=&","+o(","(puke)","(Puke)",":&",":-&",":=&","+o("),
    DOH("doh","a50c290b-fb97-404a-a3c4-2cd68e75e42c","Doh!","(doh)","(Doh)","(doh)","(Doh)"),
    ANGRY("angry","4c5f6b94-296b-4e8a-84fc-c3d7f175c1a4","Angry","(angry)","(Angry)",":@",":-@",":=@","x(","x-(","X(","X-(","x=(","X=(",";@",";-@","(angry)","(Angry)",":@",":-@",":=@","x(","x-(","X(","X-(","x=(","X=(",";@",";-@"),
    WASNTME("wasntme","8a8ac2d9-8ce3-473f-acd9-f17e8c5937f9","It wasn't me!","(wasntme)","(Wasntme)","(wm)","(Wm)","(wasntme)","(Wasntme)","(wm)","(Wm)"),
    PARTY("party","abf14cd2-0595-470e-9166-c02f7dde12ad","Party","(party)","(Party)","<O)","<o)","<:o)","(party)","(Party)","<O)","<o)","<:o)"),
    WORRY("worry","2a3ac1be-39f7-491c-9c1a-8cd9eaecee32","Worried","(worry)","(Worry)",":S",":s",":-s",":-S",":=s",":=S","(worry)","(Worry)",":S",":s",":-s",":-S",":=s",":=S","(worried)","(Worried)"),
    MMM("mmm","a3f65095-ba03-4a45-84ed-d9231468665b","Mmmmm…","(mm)","(Mm)","(mmm)","(Mmm)","(mmmm)","(Mmmm)","(mm)","(Mm)","(mmm)","(Mmm)","(mmmm)","(Mmmm)"),
    NERDY("nerdy","fbded7cb-0bad-4603-b739-b37305a6eaad","Nerdy","(nerd)","(Nerd)","8-|","B-|","8|","B|","8=|","B=|","(nerd)","(Nerd)","8-|","B-|","8|","B|","8=|","B=|","(nerdy)","(Nerdy)"),
    LIPSSEALED("lipssealed","df781c34-4c9b-4d39-9f8f-6b13ea70061f","My lips are sealed",":x",":-x",":X",":-X",":#",":-#",":=x",":=X",":=#",":x",":-x",":X",":-X",":#",":-#",":=x",":=X",":=#","(lipssealed)","(Lipssealed)"),
    DEVIL("devil","10b17da0-7b92-4098-ab1a-69b53394563e","Devil","(devil)","(Devil)","(6)","(devil)","(Devil)","(6)"),
    ANGEL("angel","8a388a15-fa48-4d0f-a9e5-cab4ab494102","Angel","(angel)","(Angel)","(A)","(a)","(angel)","(Angel)","(A)","(a)"),
    ENVY("envy","2ee49ce0-3547-4ef8-8d25-3c27659e23ef","Envy","(envy)","(Envy)","(V)","(v)","(envy)","(Envy)","(V)","(v)"),
    MAKEUP("makeup","8d86986e-8e0f-4d33-98ff-1597feb87bf5","Make-up","(makeup)","(Makeup)","(kate)","(Kate)","(makeup)","(Makeup)","(kate)","(Kate)"),
    MOVEMBER("movember","5d3575c7-b90a-42de-9e42-aa456e906e05","Movember","(movember)","(Movember)","(mo)","(Mo)","(november)","(November)","(moustache)","(Moustache)","(mustache)","(Mustache)","(bowman)","(Bowman)",":{","(movember)","(Movember)","(mo)","(Mo)","(november)","(November)","(moustache)","(Moustache)","(mustache)","(Mustache)","(bowman)","(Bowman)",":{"),
    THINK("think","98a94905-5be0-4970-b90e-792c8e94cd2b","Thinking","(think)","(Think)",":-?",":?",":=?","*-)","(think)","(Think)",":-?",":?",":=?","*-)"),
    ROFL("rofl","94c4b9fb-4d98-4bf4-a20a-3359ee2cdb75","Rolling on the floor laughing","(rofl)","(Rofl)","(rotfl)","(Rotfl)","(rofl)","(Rofl)","(rotfl)","(Rotfl)"),
    HAPPY("happy","96530617-f66c-44b3-a0b6-5e35c2cf70c8","Happy","(happy)","(Happy)","(happy)","(Happy)"),
    SMIRK("smirk","764b0a29-05f3-40a0-8017-fadbdb57032a","Smirking","(smirk)","(Smirk)","(smirk)","(Smirk)"),
    NOD("nod","2ec177ee-7004-417c-8f68-3386527c0ace","Nodding","(nod)","(Nod)","(nod)","(Nod)"),
    SHAKE("shake","e981c8fc-41de-4b1c-8741-e3f48b3fa064","Shake","(shake)","(Shake)","(shake)","(Shake)"),
    WAITING("waiting","2c79c62b-ce20-43ea-900b-cc5bcb44dbc9","Waiting","(waiting)","(Waiting)","(forever)","(Forever)","(impatience)","(Impatience)","(waiting)","(Waiting)","(forever)","(Forever)","(impatience)","(Impatience)"),
    EMO("emo","1299d1cc-bf60-44f1-9947-2dcdd886030b","Emo","(emo)","(Emo)","(emo)","(Emo)"),
    FINGERSCROSSED("fingerscrossed","776da219-95f7-43b9-9913-ddc7bc32e684","Fingers crossed","(yn)","(Yn)","(fingers)","(Fingers)","(fingerscrossed)","(Fingerscrossed)","(crossedfingers)","(Crossedfingers)","(yn)","(Yn)","(fingers)","(Fingers)","(fingerscrossed)","(Fingerscrossed)","(crossedfingers)","(Crossedfingers)"),
    HI("hi","1fa9928d-76bf-4d32-bc64-2a03dde36289","Hi","(wave)","(Wave)","(hi)","(Hi)","(bye)","(BYE)","(Bye)","(Hi)","(HI)","(wave)","(Wave)","(hi)","(Hi)","(HI)","(bye)","(Bye)","(BYE)"),
    FACEPALM("facepalm","46a0a8f8-0b72-4c5b-b1c6-fde7f03fd6ae","Facepalm","(facepalm)","(Facepalm)","(fail)","(Fail)","(facepalm)","(Facepalm)","(fail)","(Fail)"),
    WAIT("wait","a3186d86-6736-499b-a071-cddc4776cd10","Wait","(wait)","(Wait)","(wait)","(Wait)"),
    GIGGLE("giggle","1705c516-7e4d-4544-b264-165d29016908","Giggle","(chuckle)","(Chuckle)","(giggle)","(Giggle)","(chuckle)","(Chuckle)","(giggle)","(Giggle)"),
    CLAP("clap","73f69d48-8337-4a6d-9080-bef4d3471f8e","Clapping","(clap)","(Clap)","(clap)","(Clap)"),
    WHEW("whew","22f79111-3e69-401a-b86b-66cf2d51a4f7","Relieved","(whew)","(Whew)","(phew)","(Phew)","(whew)","(Whew)","(phew)","(Phew)"),
    HIGHFIVE("highfive","96835320-3fc1-4906-beda-4514a1f7e8cf","High five","(highfive)","(Highfive)","(hifive)","(Hifive)","(h5)","(H5)","(highfive)","(Highfive)","(hifive)","(Hifive)","(h5)","(H5)"),
    TMI("tmi","a407add4-959c-4ae5-8022-d91023873f9b","Too much information","(tmi)","(Tmi)","(tmi)","(Tmi)"),
    CALL("call","b1c9042e-91a3-4359-8f82-901d89e9b35a","Call","(call)","(Call)","(T)","(t)","(call)","(Call)","(T)","(t)"),
    HEADBANG("headbang","661871dc-55fd-4399-a17e-6769146c7296","Banging head on wall","(headbang)","(Headbang)","(banghead)","(Banghead)","(headbang)","(Headbang)","(banghead)","(Banghead)"),
    IDEA("idea","cfa1f733-59a1-4d1b-b549-cdec2a24b77b","Idea","(idea)","(Idea)",":i",":I","*-:)","(idea)","(Idea)",":i",":I","*-:)"),
    LALALA("lalala","a641673d-2bd9-4425-9b52-5fbc39628266","Lalala","(lalala)","(Lalala)","(lalalala)","(Lalalala)","(lala)","(Lala)","(notlistening)","(Notlistening)","(lalala)","(Lalala)","(lalalala)","(Lalalala)","(lala)","(Lala)","(notlistening)","(Notlistening)"),
    PUNCH("punch","c4fbef65-4b05-4e50-9f92-5566cec865fe","Punch","(punch)","(Punch)","*|","*-|","(punch)","(Punch)","*|","*-|"),
    ROCK("rock","26ec0fb1-8495-48a0-a113-f8014957ad78","Rock","(rock)","(Rock)","(rock)","(Rock)"),
    SWEAR("swear","ff185987-b3ad-427d-b4fe-c26bcb6714ed","Swearing","(swear)","(Swear)","(swear)","(Swear)"),
    TALK("talk","f99d820b-2dae-4145-a8ab-a9358a8f729b","Talking","(talk)","(Talk)","(talk)","(Talk)"),
    TALKTOTHEHAND("talktothehand","9499eaf1-36f1-4486-b8ed-8c024bcca53d","Talk to the hand","(talktothehand)","(Talktothehand)","(talktothehand)","(Talktothehand)"),
    SARCASTIC("sarcastic","e01c8eae-ef36-4193-b629-d4624af5daa2","Sarcastic","(sarcastic)","(Sarcastic)","(sarcasm)","(Sarcasm)","(slowclap)","(Slowclap)"),
    LISTENING("listening","02977a1b-0000-4349-acc0-dde4a2c93858","Listening","(listening)","(Listening)"),
    SLAP("slap","31bedc7f-fd21-471a-9b38-d3ef9dd57cfb","Slap","(slap)","(Slap)","(thappad)","(Thappad)"),
    WHISTLE("whistle","9b325dc7-2ba4-4148-9cfd-67cd9424f8bb","Whistle","(whistle)","(Whistle)","(seeti)","(Seeti)"),
    DONTTALKTOME("donttalktome","f0870593-e1a2-4bb0-86f6-d3d63d3af666","Don't talk to me","(donttalk)","(Donttalk)","(donttalktome)","(Donttalktote)"),
    NAZAR("nazar","b86f9dbd-be0a-4550-a640-bd2155e4521c","Blessing","(nazar)","(Nazar)"),
    BANDIT("bandit","bbeffe53-ce81-43a8-8a82-4e8beac07afb","Bandit","(bandit)","(Bandit)","(bandit)","(Bandit)"),
    LEARN("learn","1f1419ee-0c8b-4096-aa85-4f4266010862","Global Learning","(learn)","(Learn)"),
    LIPS("lips","eda275e3-f970-40ba-9d91-26a8ccb7e6d2","Kissing lips","(lips)","(Lips)"),
    HEART("heart","bfb91b6c-0b33-42b3-80cd-558544b032ea","Heart","(heart)","(Heart)","(h)","(H)","(l)","(L)","<3","(heart)","(Heart)","(h)","(H)","(l)","(L)","<3"),
    BROKENHEART("brokenheart","d8a25d66-daac-42a0-a2e1-eb75d10009bb","Broken heart","(u)","(U)","(brokenheart)","(Brokenheart)","(u)","(U)","(brokenheart)","(Brokenheart)"),
    JOY("joy","5858f185-9685-47e2-a973-f6c73d488883","Joy","(joy)","(Joy)","(joy)","(Joy)"),
    ANGER("anger","939e0512-00e4-4ae7-9e99-9468979ebe59","Anger","(anger)","(Anger)","(anger)","(Anger)"),
    SADNESS("sadness","a38afebe-dde5-4e6b-acd1-7d8fa311ec95","Sadness","(sadness)","(Sadness)","(sadness)","(Sadness)"),
    DISGUST("disgust","3854bab7-1244-4d86-8af3-196cc6f817c5","Disgust","(disgust)","(Disgust)","(disgust)","(Disgust)"),
    FEAR("fear","ce316d85-b136-40ff-b21a-e2c8cce5719b","Fear","(fear)","(Fear)","(fear)","(Fear)"),
    YES("yes","15fab22e-5d4d-43f6-9f42-f072ca5c31ea","Yes","(y)","(Y)","(yes)","(Yes)","(y)","(Y)","(yes)","(Yes)"),
    NO("no","e6c2255c-c62b-4d9d-bb6e-6057e2f96b74","No","(n)","(N)","(no)","(No)","(n)","(N)","(no)","(No)"),
    OK("ok","8e16c7d0-d1d4-457e-b1a4-5ed9e8433d90","OK","(ok)","(OK)","(oK)","(Ok)","(okay)","(Okay)","(ok)","(OK)","(oK)","(Ok)","(okay)","(Okay)"),
    HANDSHAKE("handshake","0bf0c62f-acc4-447c-8559-76cfec86f5d5","Handshake","(handshake)","(Handshake)","(handshake)","(Handshake)"),
    FISTBUMP("fistbump","901fc2a4-2938-4965-8f25-ebadd4227675","Fist bump","(fistbump)","(Fistbump)","=ƎE=","p#d","(fistbump)","(Fistbump)","=ƎE=","p#d"),
    PRAYING("praying","17971ad2-ff4e-4f25-927e-374790fab0fe","Praying","(pray)","(Pray)","(praying)","(Praying)","_/\\_","(pray)","(Pray)","(praying)","(Praying)","_/\\_","(namaste)","(Namaste)"),
    POKE("poke","3f61aa7e-0dac-41ca-9133-1925875fa834","Poke","(poke)","(Poke)","(nudge)","(Nudge)","(poke)","(Poke)","(nudge)","(Nudge)"),
    VICTORY("victory","590d4bda-8cb2-4c3c-a525-b8fd11cf063e","Victory sign","(victory)","(Victory)","(victory)","(Victory)"),
    HANDSINAIR("handsinair","037d6a10-62ac-4694-ad30-b645c07e1ce3","Hands celebrating","(celebrate)","(Celebrate)","(celebration)","(Celebration)","(handsinair)","(Handsinair)","(hia)","(Hia)","(celebrate)","(Celebrate)","(celebration)","(Celebration)","(handsinair)","(Handsinair)","(hia)","(Hia)"),
    NATURESCALL("naturescall","035d0837-6ba9-4dfd-bdeb-22071ac63242","Nature's call","(ek)","(Ek)","(eK)","(EK)","(naturescall)","(NaturesCall)"),
    MUSCLE("muscle","e44e4ece-c815-4495-84dd-ecdfb5f894a2","Muscle","(flex)","(Flex)","(muscle)","(Muscle)","(flex)","(Flex)","(muscle)","(Muscle)"),
    MAN("man","8cdc4171-9beb-4998-bce3-9ee2a3188d62","Man","(man)","(Man)","(z)","(Z)","(male)","(Male)","(boy)","(Boy)","(man)","(Man)","(z)","(Z)","(male)","(Male)","(boy)","(Boy)"),
    WOMAN("woman","9b5efbe8-4f99-426f-a3cd-1507e9954237","Woman","(woman)","(Woman)","(x)","(X)","(female)","(Female)","(girl)","(Girl)","(woman)","(Woman)","(x)","(X)","(female)","(Female)","(girl)","(Girl)"),
    BOW("bow","e89d21f2-945a-4099-9e53-d03cd9647f66","Bowing","(bow)","(Bow)","(bow)","(Bow)"),
    GOTTARUN("gottarun","fe9ed6a7-94de-4a88-8706-b8259d6ed177","Got to run","(gottarun)","(Gottarun)","(gtr)","(GTR)","(Gtr)","(run)","(Run)","(gottarun)","(Gottarun)","(gtr)","(GTR)","(Gtr)","(run)","(Run)"),
    STOP("stop","2a0d3fa1-fb92-4474-9607-d14acc78b30d","Stop","(stop)","(Stop)","(!)","(stop)","(Stop)","(!)"),
    DANCE("dance","f0c130d2-1fdf-45d2-b82e-1d90237c001c","Dancing","\\o/","\\:D/","\\:d/","(dance)","(Dance)","\\o/","\\:D/","\\:d/","(dance)","(Dance)"),
    DISCODANCER("discodancer","bbce65e8-993c-4a47-9da1-c76115919021","Disco dancer","(disco)","(Disco)","(discodancer)","(Discodancer)"),
    BHANGRA("bhangra","19b5d70c-6759-4745-843b-04b237953411","Bhangra","(bhangra)","(Bhangra)"),
    ZOMBIE("zombie","d18b0828-0f43-4d42-86bf-33cfad852510","Zombie","(zombie)","(Zombie)"),
    BERTLETT("bertlett","a8e5860e-a33c-424e-9543-d373e2c98393","Man playing football","(bartlett)","(Bartlett)","(football)","(Football)","(soccer)","(Soccer)","(so)","(So)","(bartlett)","(Bartlett)","(football)","(Football)","(soccer)","(Soccer)","(so)","(So)"),
    FOOTBALLFAIL("footballfail","a01f4402-0ac3-458e-a5ff-53c7eb5360f4","Football fail","(footballfail)","(Footballfail)"),
    PULLSHOT("pullshot","0fd6fb69-234f-4fdd-a0fa-6fbf7c00af95","Pull shot","(pullshot)","(PullShot)","(shot)","(Shot)","(chauka)","(Chauka)"),
    BOWLED("bowled","50c4fc6d-cc94-4cf0-862b-fb3b09a0c3b2","Bowled","(bowled)","(Bowled)","(out)","(Out)","(wicket)","(Wicket)"),
    BIKE("bike","8fd8729d-144c-4c6b-9926-ab8a5c7de798","Bicycle","(bike)","(Bike)","(bicycle)","(Bicycle)","(sander)","(Sander)","(bike)","(Bike)","(bicycle)","(Bicycle)","(sander)","(Sander)"),
    SURYANNAMASKAR("suryannamaskar","2db60641-5f8c-41cc-8c0e-2c17be3400db","Suryan Namaskar","(suryannamaskar)","(SuryanNamaskar)"),
    YOGA("yoga","7fd3b5e5-4c3b-4b27-9a26-ba7aa019c90b","Yoga","(yoga)","(Yoga)"),
    NINJA("ninja","abfc5c9d-d2a9-4282-b014-5d788ce65782","Ninja","(ninja)","(Ninja)","(J)","(j)","(ninja)","(Ninja)","(J)","(j)"),
    SHOPPING("shopping","b3ee2685-656b-4d74-80e6-321f1482b83e","Girl shopping","(shopping)","(Shopping)","(shopper)","(Shopper)"),
    MUSCLEMAN("muscleman","946cbcf6-ce03-4bfb-8998-a6ef093ab395","Muscle and fat guy","(muscleman)","(Muscleman)","(fatguy)","(Fatguy)"),
    SKIPPING("skipping","988b6abd-6e9d-4429-a3e0-bc6ee09a03a7","Skipping","(skipping)","(Skipping)"),
    BOLLYLOVE("bollylove","b7b32bbe-d5e2-43d2-ba95-fa3022b9d33d","In love pose","(bollylove)","(Bollylove)"),
    CHAPPAL("chappal","bc8df1b6-2ad4-460d-9596-79d064f872b2","Slipper","(chappal)","(Chappal)","(slipper)","(Slipper)"),
    NAHI("nahi","f6a1ca0d-0424-4048-b004-05984b455143","No!","(nahi)","(Nahi)","(naa)","(Naa)"),
    PROMISE("promise","ef3a7643-2e21-4645-a59d-6e371a037db4","Promise","(promise)","(Promise)","(kasamse)","(Kasamse)"),
    KAANPAKADNA("kaanpakadna","442e9cfc-27fe-4bc4-9a05-c998c3c0a06c","Sorry","(kaanpakadna)","(KaanPakadna)","(sorry)","(Sorry)","(maafi)","(Maafi)"),
    COMPUTERRAGE("computerrage","c3c186da-1eb6-4d93-8104-410ddf1cf727","Computer rage","(computerrage)","(Computerrage)","(typingrage)","(Typingrage)"),
    CAT("cat","83ded3cb-f32a-4a31-ad2d-270b0575ee1f","Cat","(cat)","(Cat)",":3","(@)","(meow)","(Meow)","(cat)","(Cat)",":3","(@)","(meow)","(Meow)"),
    DOG("dog","5d5b225e-3495-4ac6-8fd5-0158c14c4df2","Dog","(dog)","(Dog)",":o3","(&)","(dog)","(Dog)",":o3","(&)"),
    HUG("hug","d9fb4c21-7584-4e1b-84ae-931834f7354c","Hug","(hug)","(Hug)","(bear)","(Bear)","(hug)","(Hug)","(bear)","(Bear)"),
    HEIDY("heidy","bde41de3-03d2-491d-b710-f917ba311630","Squirrel","(heidy)","(Heidy)","(squirrel)","(Squirrel)","(heidy)","(Heidy)","(squirrel)","(Squirrel)"),
    DONKEY("donkey","f70b126a-f054-4909-b379-12b92afc4012","Donkey","(donkey)","(Donkey)","(gadha)","(Gadha)"),
    SNAIL("snail","acc1aecf-2615-4709-8987-cfa75a894c5a","Snail","(snail)","(Snail)","(sn)","(SN)","(Sn)","(snail)","(Snail)","(sn)","(SN)","(Sn)"),
    FLOWER("flower","cc2c4f5d-9ed1-4d82-b04e-118b9452ce4c","Flower","(F)","(f)","(flower)","(Flower)","(F)","(f)","(flower)","(Flower)"),
    GOODLUCK("goodluck","01f466d9-5ba8-4282-939a-5a71f7ef2b68","Good luck","(goodluck)","(Goodluck)","(gl)","(GL)","(Gl)","(goodluck)","(Goodluck)","(gl)","(GL)","(Gl)"),
    SUN("sun","05023b9f-1526-459c-9a22-4b66e607e499","Sun","(sun)","(Sun)","(#)","(sun)","(Sun)","(#)"),
    ISLAND("island","b519f693-abf5-48ab-a261-57be8e18eaaa","Island","(island)","(Island)","(ip)","(Ip)","(island)","(Island)","(ip)","(Ip)"),
    RAIN("rain","2e0ee67b-b7b7-4e72-af82-f70903716518","Raining","(rain)","(Rain)","(london)","(London)","(st)","(ST)","(St)","(rain)","(Rain)","(london)","(London)","(st)","(ST)","(St)"),
    UMBRELLA("umbrella","e2e74d31-b6e0-4508-a36a-bbb4684c7b26","Umbrella","(umbrella)","(Umbrella)","(um)","(Um)","(umbrella)","(Umbrella)","(um)","(Um)"),
    RAINBOW("rainbow","8abb9f5f-0cd6-441f-aff8-a7773fd50674","Rainbow","(rainbow)","(Rainbow)","(r)","(R)","(rainbow)","(Rainbow)","(r)","(R)"),
    STAR("star","43ec8bab-78e5-4400-8914-6fd1c5f51b0b","Star","(*)","(star)","(Star)","(*)","(star)","(Star)"),
    TUMBLEWEED("tumbleweed","0ab38a3a-490b-4bdc-87b5-2a187f12983d","Tumbleweed","(tumbleweed)","(Tumbleweed)","(tumbleweed)","(Tumbleweed)"),
    PIZZA("pizza","ec67b4f8-6f07-43b8-839a-14aa5cfca341","Pizza","(pi)","(Pi)","(pizza)","(Pizza)","(pi)","(Pi)","(pizza)","(Pizza)"),
    CAKE("cake","4be8c231-1211-4082-9412-b3c179e6fa90","Cake","(^)","(cake)","(Cake)","(^)","(cake)","(Cake)"),
    COFFEE("coffee","771efbba-f9b2-456a-a698-a5fa20ff1c38","Coffee","(coffee)","(Coffee)","(c)","(C)","(coffee)","(Coffee)","(c)","(C)"),
    BEER("beer","61abe0e3-93d6-438b-a6a5-ec3223525ae7","Beer","(beer)","(Beer)","(bricklayers)","(Bricklayers)","(B)","(b)","(beer)","(Beer)","(bricklayers)","(Bricklayers)","(B)","(b)"),
    DRINK("drink","461b7bfe-5412-4116-8f54-fa3a994bfba4","Drink","(d)","(D)","(drink)","(Drink)","(d)","(D)","(drink)","(Drink)"),
    CHEESE("cheese","85ddfb1d-9c8c-4cea-b22e-62e38a1ce62c","Cheese","(cheese)","(Cheese)","(stink)","(Stink)"),
    CHAI("chai","1e42be33-39a7-4cfd-bad9-a55bf61f0508","Tea","(chai)","(Chai)","(tea)","(Tea)"),
    TURKEY("turkey","8b0ac75f-9ba6-4f41-bc2e-3e90c04d45ee","Dancing Thanksgiving turkey","(turkey)","(Turkey)","(turkeydance)","(Turkeydance)","(thanksgiving)","(Thanksgiving)"),
    TANDOORICHICKEN("tandoorichicken","59decf20-8d71-4a19-9592-d60e7a74445a","Tandoori chicken","(tandoori)","(Tandoori)","(tandoorichicken)","(TandooriChicken)"),
    LADDU("laddu","dad04300-4f86-4694-b53e-d313d7fdea6f","Sweet","(laddu)","(Laddu)"),
    BELL("bell","df62539b-5cbe-4fbe-9e9d-11ae9f72ff00","Bell","(bell)","(Bell)","(ghanta)","(Ghanta)"),
    DIYA("diya","573e5133-39c6-4f93-b44c-0c2611c47fc5","Tealight","(diwali)","(Diwali)","(diya)","(Diya)"),
    FIREWORKS("fireworks","4b8e43b4-7c3a-4c24-accd-02e01e59dee2","Fireworks","(fireworks)","(Fireworks)"),
    TUBELIGHT("tubelight","11455f38-ca73-44b8-a14f-f925d59ffd05","Tubelight","(tubelight)","(Tubelight)"),
    CANYOUTALK("canyoutalk","05cff5e0-6d3a-4b46-901d-5f646e91d6de","Can you talk?","(canyoutalk)","(Canyoutalk)","(!!)"),
    CAMERA("camera","636fb0bf-a6e6-4dda-9a3a-1dd8989e95bb","Camera","(camera)","(Camera)","(p)","(P)","(camera)","(Camera)","(p)","(P)"),
    PLANE("plane","17aba7ca-5491-47fc-b0e1-68ddee14500b","Plane","(plane)","(Plane)","(ap)","(Ap)","(airplane)","(Airplane)","(aeroplane)","(Aeroplane)","(aircraft)","(Aircraft)","(jet)","(Jet)","(plane)","(Plane)","(ap)","(Ap)","(airplane)","(Airplane)","(aeroplane)","(Aeroplane)","(aircraft)","(Aircraft)","(jet)","(Jet)"),
    CAR("car","329ce829-5bb1-4cd5-b02d-5fb75c7e5f07","Car","(car)","(Car)","(au)","(Au)","(car)","(Car)","(au)","(Au)"),
    RICKSHAW("rickshaw","27f6625a-3717-4664-9426-c02c6deddadb","Rickshaw","(rickshaw)","(Rickshaw)","(rikshaw)","(Rikshaw)","(ricksha)","(Ricksha)"),
    COMPUTER("computer","2cc97f86-ed9a-49ed-ad14-50f10db762e9","Computer","(computer)","(Computer)","(co)","(Co)","(pc)","(Pc)","(computer)","(Computer)","(co)","(Co)","(pc)","(Pc)"),
    WFH("wfh","00a4d8e1-f23c-4c1a-b976-2b91512712ec","Working from home","(wfh)","(Wfh)","(@h)","(@H)","(wfh)","(Wfh)","(@h)","(@H)"),
    BRB("brb","0eb73292-97d5-485a-a223-2400e77a2870","Be right back","(brb)","(Brb)","(berightback)","(Berightback)","(brb)","(Brb)","(berightback)","(Berightback)"),
    GAMES("games","8ec10796-a6cd-47a1-84e4-c936cc70bb06","Games","(games)","(Games)","(ply)","(PLY)","(Ply)","(play)","(Play)","(playbox)","(Playbox)","(games)","(Games)","(ply)","(PLY)","(Ply)","(play)","(Play)","(playbox)","(Playbox)"),
    PHONE("phone","0d8885cd-2c25-4648-b13d-8ca6928b1854","Phone","(mp)","(Mp)","(ph)","(Ph)","(phone)","(Phone)","(mp)","(Mp)","(ph)","(Ph)","(phone)","(Phone)"),
    HOLDON("holdon","f3863ab2-ae02-44c8-bb83-b271a3eab3f5","Hold on","(holdon)","(Holdon)","(w8)","(W8)","(holdon)","(Holdon)","(w8)","(W8)"),
    LETSMEET("letsmeet","a4ec48d9-fd0c-4b8a-b6ad-a74e744b885d","Let's meet","(letsmeet)","(Letsmeet)","(s+)","(S+)","(calendar)","(Calendar)","(letsmeet)","(Letsmeet)","(s+)","(S+)","(calendar)","(Calendar)"),
    MAIL("mail","31412c11-7edc-49f2-8413-949b7e0b18f3","You have mail","(e)","(E)","(m)","(M)","(mail)","(Mail)","(e)","(E)","(m)","(M)","(mail)","(Mail)"),
    CONFIDENTIAL("confidential","f4d32e66-650b-4108-aafd-924825013356","Confidential","(confidential)","(Confidential)","(qt)","(QT)","(Qt)","(confidential)","(Confidential)","(qt)","(QT)","(Qt)"),
    BOMB("bomb","7d2643ff-57f4-4972-b66d-4cc01548bd72","Bomb","(bomb)","(Bomb)","(explosion)","(Explosion)","(explode)","(Explode)","@=","(bomb)","(Bomb)","(explosion)","(Explosion)","(explode)","(Explode)","@="),
    CASH("cash","982be1e5-3088-4277-aeae-487f82396738","Cash","(cash)","(Cash)","(mo)","(Mo)","($)","(cash)","(Cash)","($)"),
    MOVIE("movie","3244f024-7a8f-4396-944c-7bed2f841dae","Movie","(~)","(film)","(Film)","(movie)","(Movie)","(~)","(film)","(Film)","(movie)","(Movie)"),
    MUSIC("music","1b55f32f-0f93-495e-9e8b-6ba25de331ca","Music","(music)","(Music)","(8)","(music)","(Music)","(8)"),
    TIME("time","5cd18fd4-7235-48b9-871f-0210e1e1e7df","Time","(o)","(O)","(time)","(Time)","(clock)","(Clock)","(0)","(o)","(O)","(time)","(Time)","(clock)","(Clock)","(0)"),
    WHATSGOINGON("whatsgoingon","a31a6430-a1e7-409d-99cf-19548004d94a","What's going on?","(whatsgoingon)","(Whatsgoingon)","(!!?)","(whatsgoingon)","(Whatsgoingon)","(!!?)"),
    SKYPE("skype","294b7dd2-c779-4c5d-95d8-d33f8ef4b727","Skype","(skype)","(Skype)","(ss)","(Ss)","(skype)","(Skype)","(ss)","(Ss)"),
    MLT("mlt","781498ca-caed-4635-8841-90734c750b23","Smiling man with glasses","(malthe)","(Malthe)","(malthe)","(Malthe)","(mlt)","(Mlt)"),
    TAUR("taur","b8521fee-d051-452f-b68f-795e6e08bb97","Bald man with glasses","(tauri)","(Tauri)","(tauri)","(Tauri)","(taur)","(Taur)"),
    TOIVO("toivo","69dd4c9a-e67a-406a-9607-0f6e8620a8ab","A man and his dog","(toivo)","(Toivo)","(toivo)","(Toivo)"),
    PRIIDU("priidu","5445cae9-e4f3-4dcd-b08e-943688740ebe","Man taking a photo","(zilmer)","(Zilmer)","(zilmer)","(Zilmer)","(priidu)","(Priidu)"),
    OLIVER("oliver","d080e378-97af-46d1-ba74-666eed290686","Man saying come on","(oliver)","(Oliver)","(oliver)","(Oliver)"),
    POOLPARTY("poolparty","fdfe5d6c-36d2-4973-9c2a-16b02bf4cde0","Pool party","(poolparty)","(Poolparty)","(hrv)","(Hrv)","(poolparty)","(Poolparty)","(hrv)","(Hrv)"),
    MOONING("mooning","6c5550ee-02fb-4e9f-a11e-6428b861fe71","Mooning","(mooning)","(Mooning)","(mooning)","(Mooning)"),
    DRUNK("drunk","6ed139a7-ae0b-4e05-8836-f799ea7d63be","Drunk","(drunk)","(Drunk)","(drunk)","(Drunk)"),
    SMOKE("smoke","eec11da6-b0da-40f4-9417-08149bfbf7c9","Smoking","(smoking)","(Smoking)","(smoke)","(Smoke)","(ci)","(Ci)","(smoking)","(Smoking)","(smoke)","(Smoke)","(ci)","(Ci)"),
    BUG("bug","e73b1fa3-880a-4f06-95ea-f197baeb0d22","Bug","(bug)","(Bug)","(bug)","(Bug)"),
    SHEEP("sheep","7c309156-ba1c-49f1-8921-58df1d3f5427","Sheep","(sheep)","(Sheep)","(bah)","(Bah)","(sheep)","(Sheep)","(bah)","(Bah)"),
    WIN10("win10","aac458bd-e0f0-441c-a5ca-ddb0531ab829","Windows 10","(win10)","(Win10)","(ninjacat)","(Ninjacat)","(windows)","(Windows)","(trex)","(Trex)","(windows10)","(Windows10)","(win10)","(Win10)","(ninjacat)","(Ninjacat)","(windows)","(Windows)","(trex)","(Trex)","(windows10)","(Windows10)"),
    OUTLOOK("outlook","9a27e5ba-f3dd-4ba0-8e0d-d11024f4d291","Outlook","(outlook)","(Outlook)","(outlook)","(Outlook)"),
    ACCESS("access","41c9a744-0f7b-4fc1-9944-d4a80fb58b91","Access","(access)","(Access)","(access)","(Access)"),
    BING("bing","521e6518-70d3-45be-880a-d0fb381d5caa","Bing","(bing)","(Bing)","(bing)","(Bing)"),
    EXCEL("excel","3ce63a1f-5578-460d-a842-5f3f164eb311","Excel","(excel)","(Excel)","(excel)","(Excel)"),
    INTERNETEXPLORER("internetexplorer","666d9a64-ce10-41b0-97da-7d09b8c5859c","Internet Explorer","(internetexplorer)","(Internetexplorer)","(ie)","(Ie)","(IE)","(internetexplorer)","(Internetexplorer)","(ie)","(Ie)","(IE)"),
    MICROSOFT("microsoft","5285b407-cbd6-4a29-a4ae-ceb5831fc02b","Microsoft","(microsoft)","(Microsoft)","(ms)","(Ms)","(MS)","(microsoft)","(Microsoft)","(ms)","(Ms)","(MS)"),
    ONENOTE("onenote","ad6b8dd5-e603-4d5a-b844-1ca8402c250e","OneNote","(onenote)","(Onenote)","(onenote)","(Onenote)"),
    ONEDRIVE("onedrive","dfdc0534-5e54-46b4-8f7c-1318a29e8333","OneDrive","(onedrive)","(Onedrive)","(onedrive)","(Onedrive)"),
    POWERPOINT("powerpoint","6b225b3b-cfa2-4af4-8f53-a9ef823d84c3","PowerPoint","(powerpoint)","(Powerpoint)","(powerpoint)","(Powerpoint)"),
    PUBLISHER("publisher","36d2cf0a-53d5-438f-822e-4418ce507b34","Publisher","(publisher)","(Publisher)","(publisher)","(Publisher)"),
    SHAREPOINT("sharepoint","e5edf4e6-9950-4e95-9c97-215ae0dbb9e2","SharePoint","(sharepoint)","(Sharepoint)","(sharepoint)","(Sharepoint)"),
    SKYPEBIZ("skypebiz","d2563040-84f7-4c44-98cd-146dddf965af","Skype for Business","(skypebiz)","(Skypebiz)","(sforb)","(Sforb)","(skypebiz)","(Skypebiz)","(sforb)","(Sforb)"),
    WORD("word","e9c8504b-20cc-4e37-96ad-7b176c46f7e7","Word","(word)","(Word)","(word)","(Word)"),
    XBOX("xbox","5a0233ce-aa10-438d-8419-b701764c2eff","Xbox","(xbox)","(Xbox)","(xbox)","(Xbox)"),
    WTF("wtf","5a43ccc7-e032-4394-9e74-f9cc9ff34db7","WTF ...","(wtf)","(Wtf)"),
    FINGER("finger","8aef7767-65b1-4f02-99b7-c5e392f4c4b0","Finger","(finger)","(Finger)"),
    GHOST("ghost","92dbe167-5e50-47e7-8778-91aa77242624","Ghost","(ghost)","(Ghost)"),
    VAMPIRE("vampire","04f72181-419f-4620-b6d0-1fca2534ac7f","Vampire","(vampire)","(Vampire)"),
    SKULL("skull","8e4b812b-d2ca-4f7a-b513-f36aac834100","Skull","(skull)","(Skull)"),
    PUMPKIN("pumpkin","9d20af2f-c7b9-4605-98ba-a2a400382cac","Pumpkin","(pumpkin)","(Pumpkin)","(halloween)","(Halloween)"),
    LADYVAMPIRE("ladyvampire","42f31ee1-4616-4e70-862a-6c0b171dd26e","Lady vampire","(ladyvamp)","(Ladyvamp)","(ladyvampire)","(Ladyvampire)"),
    ABE("abe","28f516c6-9074-453b-b076-90bc11a39e3a","Abe","(abe)","(Abe)","(abey)","(Abey)"),
    GOLMAAL("golmaal","ffd9ba35-88e8-4014-876a-38cf968b5e53","Confusion","(golmaal)","(Golmaal)"),
    OYE("oye","ba561f59-eeb4-4f31-8bc6-e721dcabc877","Oye","(oye)","(Oye)"),
    POOP("poop","dd87d14a-3cf1-4a56-8b70-e857e0b69253","Pile of poo","(poop)","(Poop)"),
    KYA("kya","9a6af308-7dd8-4d2b-9f20-4063843c2eba","What!","(kya)","(Kya)"),
    ONTHELOO("ontheloo","f021942c-3772-4ce6-b426-8f2574745fe1","On the loo","(ontheloo)","(Ontheloo)","(onloo)","(Onloo)","(nr2)","(Nr2)","(twittering)","(Twittering)","(verybusy)","(Verybusy)"),
    NEIL("neil","a055e5b0-5e6a-4b4d-8b7d-3bb5d7d0b210","Neil","(neil)","(Neil)","(feetontable)","(Feetontable)"),
    SANTAMOONING("santamooning","9515d561-42d1-4c1d-b665-16b8c72c918d","Santa mooning","(santamooning)","(Santamooning)","(mooningsanta)","(Mooningsanta)"),
    GIFT("gift","9b399845-5842-4b5f-be0b-5f067d6070b2","Gift","(gift)","(Gift)","(g)","(G)","(gift)","(Gift)","(g)","(G)"),
    SANTA("santa","891eee5b-1d09-479f-80f7-45b6b7d52d23","Santa","(santa)","(Santa)","(xmas)","(Xmas)","(christmas)","(Christmas)"),
    XMASTREE("xmastree","cf33b2b7-335d-43d9-be26-ba35a88e601a","Xmas tree","(xmastree)","(Xmastree)","(christmastree)","(Christmastree)"),
    HANUKKAH("hanukkah","2806a529-0e59-41d0-b0aa-9a8f2daabcba","Hanukkah","(hanukkah)","(Hanukkah)"),
    CHAMPAGNE("champagne","8ff6b2c0-2819-4d39-9625-052befa38b68","Champagne","(champagne)","(Champagne)","(sparkling)","(Sparkling)"),
    PENGUIN("penguin","2597eabf-15b8-48f3-a5b6-177740013e8b","Dancing penguin","(penguin)","(Penguin)","(dancingpenguin)","(Dancingpenguin)","(penguindance)","(Penguindance)"),
    FESTIVEPARTY("festiveparty","ff29738b-be18-4b71-b140-68760c58f3a7","Festive party","(festiveparty)","(Festiveparty)","(partyxmas)","(Partyxmas)"),
    HUNGOVER("hungover","f2a2f592-8ac9-4e69-a90d-b94787658944","Morning after party","(morningafter)","(Morningafter)","(hungover)","(Hungover)"),
    HEADPHONES("headphones","3a62dadc-1e79-42f1-9dc5-96c8c159dad2","Listening to headphones","(headphones)","(Headphones)"),
    SHIVERING("shivering","176ee7a9-094d-43d4-87a2-2193796691ac","Cold shivering","(shivering)","(Shivering)","(cold)","(Cold)","(freezing)","(Freezing)");

    private String id;
    private String etag;
    private String desc;
    private List<String> shortcuts;

    Emoticon(String id, String etag, String desc, String... shortcuts) {
        this.id = id;
        this.etag = etag;
        this.desc = desc;
        this.shortcuts = Arrays.asList(shortcuts);
    }

    public String getId() {
        return this.id;
    }

    public List<String> getShortcuts() {
        return Collections.unmodifiableList(this.shortcuts);
    }

    public String getEtag() {
        return this.etag;
    }

    public String getDescription() {
        return this.desc;
    }
}
