


**NOTE**: So far `Promises` are only available in the gwtquery-1.4.0-SNAPSHOT version.

# Introduction #

The `Deferred` object, introduced in gQuery 1.4.0 is a chainable utility object created by calling the `GQuery.Deferred()` method.

It can register multiple callbacks into callback queues, invoke callback queues, and relay the success or failure state of any synchronous or asynchronous function.

The `Deferred` object is chainable, similar to the way a `GQuery` object is, but it has its own methods. After creating a Deferred object, you can use any of its methods (notify, reject, resolve, promise) by either chaining directly from the object creation or saving the object in a variable and invoking one or more methods on that variable.

The `Promise` object provides a subset of the methods of the Deferred object (then, done, fail, always, pipe) to prevent users from changing the state of the `Deferred`. It is the object returned by asynchronous processes like `Ajax.ajax()`.

The implementation GQuery `Promises` is inspired in jQuery API and is based on the [CommonJS Promises/A](http://code.google.com/p/gwtquery/wiki/Promises) and [Promise/A+](http://promises-aplus.github.io/promises-spec/) specs.


# Understanding Promises #

A Promise represents a one-time event, typically the outcome of an async task like an `Ajax` call. At first, a Promise is in a pending state. Eventually, it’s either resolved (meaning the task is done) or rejected (if the task failed).

Once a Promise is resolved or rejected, it will remain in that state forever, and its callbacks will never fire again.

But you can attach more callbacks to the Promise whenever you want (even after the Promise has been resolved/rejected), and they will fire immediately.

Plus, you can combine Promises logically into new Promises. That makes it trivially easy to write code that says, “When all of these things have happened, do this other thing.”

# Promises for Ajax #

All GQuery Ajax methods return a promise, so you could join multiple calls into a promise and call methods whenever all of then finish.

```
        Promise gettingThings = GQuery.when(
            Ajax.getJSONP(url1), 
            Ajax.getJSONP(url2)
          );
        
        gettingThings.done(new Function() {
            public void f() {
              // The json reponse of the url1 
              Properties json1 = getArgument(0, 0);
              // The json response of the url2
              Properties json2 = getArgument(1, 0);
            }
          });
        
        gettingThings.fail(new Function(){
            public void f() {
              Window.alert("error");
            }
          });

```

# Promises for Queues and Animations #

`GQuery.promise()` returns a promise which will observe all queued processes, so as you can chain effects and run functions when they finish.

```
$("div").fadeIn(800)
        .delay(1200)
        .fadeOut()
        .done(new Function() {
           public void f()  {
              Window.alert("Finished");
           }
        });
```

# Promises for asynchronous GWT (Helpers) #

Apart from the `PromiseFunction`, gquery comes with helpers for GWT asynchronous implementations:

  * Use `new PromiseReqBuilder(builder)` to execute a `RequestBuilder` and get a chainable `Promise`.
  * Use `new PromiseReqBuilderJSONP(url)` to execute a `jsonp` request and get a  `Promise`.
  * Use `new PromiseRPC<T>()` to wrap in a `Promise` a RPC service call.
  * Use `new PromiseRF(request<T>)` to fire a `RequestFactory` request and return a `Promise`

# Custom Promises #

GQuery comes with `PromiseFunction` a utility abstract class which facilitates creation of `Promises` and easy interact to its `Deferred` object.

You have to implement a function `void f(Deferred)` which will be executed just once when the promise is created. You get the deferred instance which so as you can resolve the promise.

```
  Promise doingFoo = new PromiseFunction() {
    public void f(Deferred dfd) {
      dfd.notify(1);
      dfd.resolve("doneFoo");
    }
  };

  Promise doingBar = new PromiseFunction() {
    public void f(Deferred dfd) {
      dfd.notify(2);
      dfd.resolve("doneBar");
    }
  };
  
  Promise doingAll = GQuery.when(doingFoo, doingBar);
  
  doingAll.progress(new Function() {
            public void f() {
              int progress = getArgument(0);
            }
          })
          .done(new Function() {
            public void f() {
              String doneFoo = arguments(0, 0);
              String doneBar = arguments(1, 0);
            }
          });

```

# Monitoring simultaneous async calls #

A very common case is to put together some callbacks and monitor whether all of them are resolved or any of them is rejected. The method `when()` allows to combine multiple subordinates and return one promise which will be resolved only in the case all of them succeed.


```
// Create 3 helper promises to wrap RPC callbacks
PromiseRPC<String> promise1 = new PromiseRPC<String>();
PromiseRPC<String> promise2 = new PromiseRPC<String>();
PromiseRPC<String> promise3 = new PromiseRPC<String>();

// Fire requests
greetingService.greetServer(textToServer1, promise1);
greetingService.greetServer(textToServer2, promise2);
greetingService.greetServer(textToServer3, promise3);

GQuery
      // 'when' returns a new promise which monitors all of its subordinates
      .when(promise1, promise2, promise3)
      // 'done' will be executed only in the case all promises succeed
      .done(new Function() {public void f() {
          // each promise store its result in a fixed position of the argument list
          String textFromServer1 = arguments(0, 0);
          String textFromServer2 = arguments(1, 0);
          String textFromServer3 = arguments(2, 0);
      }})
      .fail(new Function() {public void f() {
          // In the fail method we can get the message of the failed call
          Exception error =  arguments(0);
      }});
```

But `when()` also allows monitor any kind of objects, so we could mix `Promises`, `Functions`, `Effects`, `Queues` or plain objects. `done()` functions will have access to all the objects or its results depending on the case.

```
  GQuery.when(
              $("div").fadeIn(800).delay(1200).fadeOut(), 
              $("p").slideDown(400),
              "Foo",
              $$("{bar: 'foo'}",
              Ajax.getJSONP("http://whatever")
        ).done(new Function() {public void f()  {
              // promises in queues and effects return a GQuery object with the same elements
              GQuery g1 = arguments(0,0);
              GQuery g2 = arguments(1,0);
              // objects are passed unmodified to the done function
              String foo = arguments(2,0);
              Properties p1 = arguments(3,0);
              // async calls return their result
              Properties p2 = arguments(4,0);
        }});
```


# Chaining async calls: pipelining #

Sometimes it is useful to delay the execution of an asynchronous process until we have the results of a previous one. You can pipeline processes using the `Promise.then()` method. This method accept functions which can return filtered arguments, or new promises:

```

Ajax.ajax("your/web/service/getData")
    .then(new Function() {public Object f(Object...args) {
        theOriginalData = arguments(0);
        return Ajax.ajax("your/web/service/getTheFinalDataWith?data=" + theOriginalData);
    }})
    .done(new Function() {public void f() {
        theFinalData = arguments(0);
    }});
```

But the `Promise.then()` method is also useful for filtering the results of an execution, similar to `GQuery.map()` but with async callbacks:

```

Ajax.ajax("your/web/service/getData")
    .then(new Function() {public Object f(Object...args) {
        theOriginalData = args[0];
        return theOriginalData + " modified";
    }})
    .done(new Function() {public void f() {
        theFinalData = arguments(0);
    }});
```

# Declarative workflow #

Complex ajax applications code is difficult to follow due to the excessive usage of asynchronous callbacks. gQuery offers a way to maintain your GWT code in a more `declarative` way when dealing with complex asynchronous work flows:

```
  GQuery.when(doingLogin())
        .and(gettingUserconfigFromCache())
        .or(gettingUserconfigFromServer())
        .done(new Function() {public void f()  {
           Userconfig c = arguments(0);
           $("#username").text(c.getName);
        }});
```