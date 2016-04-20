# Bathtub
Sample project

-Architecture desing: MVP
-Techonogies: Retrofit2, Dagger2, Butterknife, RxJava
-Test: JUnit, Mockito

-Approach:
Project based on mvp. 
Use of presenter and interator to handle the logic in the domain layer. 
Presentation layer to display view. Here it uses an interface to communicate Presenter with Activity. 
Data layout to handle models.

-Problems and things to improve:
Retrofit was returnning a malformed Json exception due to the json provided. Looks like the GsonConverter wasnt able to read 
the json fieds without "": 

{
	hot_water: 50,
	cold_water: 10
}

I left the Retrofic logic that is catching the error.
As workaround i just get the json using a HttpUrlConnection.

So far, logic to fill the bathtub has to be improved


-Device density target: hdpi
-Time spent: 2 days
-Developed in: Android Studio 2.0
