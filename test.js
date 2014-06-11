function l(){
    console.log(arguments);
}
(function(){

    Function.prototype.method = function(name, func){
        this.prototype[name] = func;
        return this;
    }

    function test(){
        (function testErrorHandlering(){
            function t(){
                throw ('test error');
                l('the end of t')
            }
            try {
                t();
                l('try');
            } catch (e){
                l('error', e);
            } finally {
                l('finally');
            }
        })();

        (function testScope(){
            function t(){
                {
                    var a = 1;
                }
                l('a:', a);
            }
            t()
        })();

        (function test(){
            function t(){
                var a =
            }
        })();
    }
    test();


})();
