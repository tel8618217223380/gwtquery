# Introduction #

Add your content here.


# Selectors #

# Events #

## I want to use GwtQuery for event handling inside my custom widget. ##

The main rule is : "Bind your event handler once the widget is attached to the DOM".

So, when you write your own widget, the best place for binding your event handler is inside the `onLoad()` method of the widget. Don’t forget to unbind your event henadler in the `onUnload()` method to release the resources and avoid to bind two times the same function when the widget is detached and after re-attached.

```
public class MyWidget extends Widget {


@Override
  protected void onLoad() {
    //always first call the super.onLoad() in order to attach the
    //widget before you bind your event handlers.
    super.onLoad();
    
    $(this).click(new Function() {
      @Override
      public void f() {
       //put your code to execute when the user click on the widget
      }
    });
    
    //consider that your widget contains some input field and you
    //want to execute some code when an input receive the focus
    $(this).find("input").focus(new Function() {
      @Override
      public void f() {
       //put your code to execute when an input inside your widget 
       //receive the focus
      }
    });
  }

  @Override
  protected void onUnload() {
    //always 
    super.onUnload();
    $(this).unbind(Event.ONCLICK)
           .find("input").unbind(Event.ONFOCUS);
    
  }
}

```

## I want to bind an event handler for all widget of the same type, now and in the future ##
Solution : Use the `live`and `delegate`methods.

Imagine you want to make all labels (present now in the DOM or attached in the future) clickable and that they execute the same code when they are clicked.

```
//all Label widget have gwt-Label css class and so will match this selector
//we assume that we don't add the gwt-Label css class on other widget
$(".gwt-Label").live(Event.ONCLICK, new Function() {
     
      @Override
      public void f(Widget w) {
        
        Label l = (Label) w;
        //do something with the clicked label
      }
    });
      
```

What the code above means ? Each time that a Label (i.e an element having "gwt-Label" as css class) is clicked, the `Function`will be executed.