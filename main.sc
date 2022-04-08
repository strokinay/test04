require: slotfilling/slotFilling.sc
    module = sys.zb-common
require: js/utils.js
require: justTour.sc
require: justWeather.sc
require: dicts.sc
require: debug_states.sc
require: dateTime/dateTime.sc
  module = sys.zb-common
require: name/name.sc
    module = sys.zb-common
require: city/city.sc
    module = sys.zb-common


theme: /
    
    init:
        bind("postProcess", function($context) {
            $context.session.lastActiveTime = $jsapi.currentTime();
            log($context.session.lastActiveTime)
        });
 
        bind("preProcess", function($context) {
            if ($context.session.lastActiveTime) {
                var interval = $jsapi.currentTime() - $context.session.lastActiveTime;
                if (interval > 1000000) {
                    $jsapi.startSession();
                    $reactions.newSession( {message: '/start', data: {newSession:true}} );
                    $session.startNewSession = true;
                }
            }
        });

    # main menu
    state: Start
        q!: $regex</start>
        script:
            resetForecastData($session);
            if ($client.name) {
                var text = 'Приветcтвую, '+ $client.name +'! 👋 \nЯ - Марк, чат-бот турагентства Just Tour 🏖';
                $reactions.answer(text);
                $reactions.transition('/WhatDoYouWant');
            } else {
                $reactions.answer('Привет! 👋 \nЯ - Марк, чат-бот турагентства Just Tour 🏖');
                $reactions.transition('/GetName');
            }
        
    state: WhatDoYouWant
        script:
            resetForecastData($session);
        a: Что вы хотите?
        buttons:
            "Узнать погоду"
            "Заказать тур"
            
        state: IWantWeather
            q!: ~погода
            q: (первое/1/один)
            q: номер (1/один)
            go!: /Weather/Date
            
        state: IWantTour
            q!: (тур/~путевка)
            q: (второе/2/два/последнее)
            q: номер (2/два)
            go!: /Tour/City
            
    state: GetName
        a: Давайте познакомимся! Как вас зовут? 
            Если хотите остаться инкогнито, нажмите на кнопку "Главное меню".
        buttons:
            "Главное меню" -> /WhatDoYouWant

        state: CatchUserName
            q: * $Name *
            script:
                log(toPrettyString($parseTree));
                $client.name = $parseTree.value.name;
                log($client.name);
                $reactions.transition('/NiceToMeetYou');
                
        state: UndefName
            q: *
            script:
                $reactions.answer("Не вижу имени 😟 Пожалуйста, представьтесь - я наверняка пойму полную форму в именительном падеже, например, \"Сусанна\".")
                $reactions.buttons({text: "Главное меню", transition: "/WhatDoYouWant"});
                
    state: Hello
        q!: (привет*/здравствуй*/прив/хелло/салют/хола/ола/алоха/хай/хаюшкибонжур/гутен (таг/морген/абен*)/hallo/hello/hi/добр* (день/дня/вечер*/утро/утра))
        script:
            if ($client.name) {
                $reactions.answer('Приветствую, ' + $client.name + '!');
            } else {
                $reactions.answer('Приветствую!');
            }
            $reactions.transition('/WhatDoYouWant');
        
    state: Bye
        q!: (пока/$regex<пока-пока>/до свидания/всего доброго/до скорого/до скорой встречи/покеда/пакеда/бай/покасики/досвидули/чао/аривидерчи/ауфвидерз*)
        a: Всего доброго! 
            Когда понадоблюсь, просто введите /start 😉
    
    state: BackToStartGlobal
        q!: * $StopContext *
        go!: /WhatDoYouWant
    
    state: NiceToMeetYou
        a: Приятно познакомиться, {{$client.name}}!
        go!: /WhatDoYouWant
   
    state: CatchAll || noContext = true
        event!: noMatch
        random:
            a: Я не понял. Вы сказали: "{{$request.query}}", можно другими словами?
            a: Пожалуйста, перефразируйте запрос выше.
            a: Простите, мне непонятно, что вы попросили. Прошу сказать иначе.
            a: Очень интересно, но ничего не понятно... Попробуйте иными словами 🙂
        script:
            $reactions.answer('Показать вопрос ещё раз?');
            $reactions.buttons({text: "Давай", transition: $context.contextPath});
            $reactions.buttons({text: "Главное меню", transition: '/WhatDoYouWant'});