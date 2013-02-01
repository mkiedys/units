units
=====

Flexible, statically-checked experimental library for units of measurement.

This is a result of my experiments with Scala's type system.

The goal I was aiming for when working on this library was to provide units of measurement with following properties:

* values with units are unboxed (possible thanks to Scala 2.10's custom value classes)

* units can be defined by user (as opposed to implementations limiting users to seven base SI units)

* all unit checking should be done on compile time (as opposed to runtime)

* not a compiler plugin or a macro (although I may add some macro-powered syntactic sugar in the future)

* proper support for affine spaces

* easy conversion between units

The library is published under MIT License.

Why not any other implementation?
---------------------------------

All of the following implementations were either inefficient or inflexible:

* [units (compiler plugin)](https://lampsvn.epfl.ch/trac/scala/browser/compiler-plugins/units/trunk)

    * Both outdated and a compiler plugin.

* [Metascala Units](http://trac.assembla.com/metascala/browser/src/metascala/Units.scala)

* [scalax-units](https://github.com/soc/scalax-units)

* [ScalaQuantity](https://github.com/zzorn/ScalaQuantity)

    * All three above are limited to 7 SI units only, and since they were made before 2.10, values are boxed. They were the direct inspiration for this library.

* [axle.quanta](https://github.com/adampingel/axle/tree/master/core/src/main/scala/axle/quanta)

    * Allows for adding new units, but all unit checking is performed at runtime, causing a significant runtime overhead.

* [scalau](https://github.com/adrianfr/scalau)

    * A source code preprocessor. Converts all units to SI. Does not support arbitrary unit expressions, for example metre to the fourth power would be an invalid unit.

Quick showcase
==============

    import stasiak.karol.units.Units._
    import stasiak.karol.units.DefiningUnits._

    type USD = DefineUnit[_U~:_S~:_D] 
    type EUR = DefineUnit[_E~:_U~:_R] 

    implicit val EUR_to_USD = one[EUR].contains(1.25)[USD]

    import stasiak.karol.units.units.SI._
    import stasiak.karol.units.units.USCustomary._

    val priceInUSA     =  200.of[USD/square[foot]]
    val priceInGermany = 1500.of[EUR/square[metre]]

    val area = 200.of[centimetre] * 550.of[centimetre]

    val costInUSA     = priceInUSA     * area.convert[foot × foot]
    val costInGermany = priceInGermany * area.convert[metre × metre]

    println(s"You can buy tiles in Germany for ${costInGermany.mkString}.")
    println(s"You can buy tiles in USA for ${costInUSA.mkString}.")

    if(costInUSA >~ costInGermany) {
        println("Buy in Germany.")
    } else {
        println("Buy in USA.")
    }

prints:

    You can buy tiles in Germany for 16500.0 EUR.
    You can buy tiles in USA for 23680.602916761392 USD.
    Buy in Germany.

Quick Guide
===========

Defining units
--------------

You define a unit using `DefineUnit` type constructor with a type-level string as a parameter.

    import stasiak.karol.units.Units._
    import stasiak.karol.units.DefiningUnits._

    type metre = DefineUnit[_m]
    type second = DefineUnit[_s]
    type kilogram = DefineUnit[ _k ~: _g ]

All units are subtypes of trait `MUnit`. This includes type `Scalar` (alias: `_1`), which represents dimensionless unit 1.

This also automatically generates implicit names for those units: `"m"`, `"s"`, and `"kg"` respectively.

You can define a derived unit with operators `×` and `/` (the ASCII alternative for × is ><) and type-level functions `square` and `cube`:

    type newton = (metre × kilogram) / (second × second)
    type hertz  = _1 / second
    type m2     = square[metre]
    type m3     = cube[metre]

with implicit names `"kg m s^(-2)"`, `"s^(-1)"`, `"m^2"`, and `"m^3"` respectively.

You can define a related unit with conversion ratio:

    type centimetre = DefineUnit[_c~:_m]
    type kilometre = DefineUnit[_k~:_m]
    
    implicit val km_to_m = one[kilometre].contains(1000)[metre]
    implicit val m_to_cm = one[metre].contains(100)[centimetre]

This way, you have defined conversions m→km, km→m, m→cm, cm→m, m²→km², m³→km³, km²→m² etc.

Note that this does not define a conversion from kilometres to centimetres. You can do it this way:

    implicit val km_to_cm = km_to_m >> m_to_cm

You can also quickly generate conversions for kg×m→kg×cm or m/s→cm/s:

    implicit val kgm_to_kgcm = m_to_cm.times[kilogram]
    implicit val mps_to_cmps = m_to_cm.dividedBy[second]

See sources for `stasiak.karol.units.units.SI` and `stasiak.karol.units.units.USCustomary` objects for more examples.


Using values with units
-----------------------

All code below assumes the following is imported:

    import stasiak.karol.units.Units._

You can create a value with a unit:

    val length = 2.of[metre]

This value is of type `IntU[metre]`. If you used a double literal, you would receive a `DoubleU` instance:

    val length2 = 2.0.of[metre]

`IntU` and `DoubleU` are represented at runtime as unboxed `Long` and `Double` respectively.

You can add and subtract values with the same units:

    length + length2 // equals 4 m

You can multiply or divide two values:

    length * length2 // equals 4 m^2

You can also compare values with the same units:

    val area = 3.of[metre × metre]
    area > length * length2        //equals false

Note: `DoubleU` does not support equality comparison.

If you want to extract the raw scalar numeric value from the value with a unit, you can use value method:

    length.value // equals 2: Long

Other useful methods include raising to powers and getting roots:

    length.pow2             // 4 m^2
    length.pow3             // 8 m^3
    area.sqrt               // 1.7... m
    27.of[cube[metre]].cbrt // 3.0 m

Note that while using `sqrt` (respectively: `cbrt`) method will currently work for types with units to odd (respectively: not divisible by three) powers, but the resulting value will have some ill-defined type.

If you want to use an SI prefix without creating a separate unit for it, you can use more extension methods defined on numeric types:

    20.kilo[metre]  // equals 20000 m
    3.milli[second] // equals 0.003 s

Finally, to print a value with a unit, you can use `mkString` method:

    length.mkString               // equals "4 m"
    300.of[metre/second].mkString // equals "300 m s^(-1)"

It is currently not recommended though, it takes a lot of time to compile and generates awfully large classfiles. The recommended way is to use `value` method and append a unit symbol manually.

Manual unit conversion
----------------------

You can convert a value to another unit, provided there is an implicit conversion ratio in scope:

    val length = 2.of[metre]
    length.convert[centimetre]          // equals 200.0 cm

If you are converting an integer value from a unit to its subunit, you can get the results as an `IntU`:

    length.convertToInt[centimetre]     // equals 200 cm

You can apply the conversion ratio to a more complicated unit in order to replace one unit with another:

    length.represent[metre, centimetre] // equals 200.0 cm
    area.represent[metre, centimetre]   // equals 300.0 cm×m
    1.represent[metre, centimetre]      // equals 100.0 cm/m

Finally, you can apply the conversion to all occurrences of a unit inside another. The only restriction is that the unit you are converting from is a basic unit, defined with `DefineUnit`:

    area.representAll[metre, centimetre]                              // equals 30000.0 cm^2
    1.representAll[metre, centimetre]                                 // equals 1.0
    2.of[metre/second].representAll[metre, centimetre]                // equals 200.0 cm/s
    2.of[square[metre]/second].representAll[metre, centimetre]        // equals 20000.0 cm^2/s
    30000000.of[kilogram/cube[metre]].representAll[metre, centimetre] // equals 30.0 kg/cm^3

Automatic unit conversion
-------------------------

Units are automatically converted in comparisons using operators ending with a tilde:

    399.of[centimetre] >=~ 4.of[metre]   // false

If there is a integer conversion ratio between two units, then adding/subtracting two values with those units converts the sum/difference to the smaller unit:

    14.of[centimetre] + 3.of[metre]  // equals 314 cm, not 3.14 m

Affine spaces
-------------

Affine space is a space of quantities, which cannot be multiplied or added, only subtracted. The reason for that is that the zero is chosen arbitrarily. Affine spaces are used to represent temperatures, timestamps, Cartesian coordinates, potential energy, electric potential, and more. See [this article](http://math.ucr.edu/home/baez/torsors.html) for more info.

Example:

    val freezeC = 0.at[CelsiusScale] // temperature at which water freezes
    0.of[celsius_deg]                // zero difference of temperatures

Affine values have types `DoubleA[A]` and `IntA[A]`, where `A` is an affine space.

The reason that you can't add affine values is that this operation makes no sense.

    val freezeF = freezeC.convert[FahrenheitScale] // equals 32°F
    freezeC + freezeC                              // 0°C + 0°C == does not compile
    freezeF + freezeF                              // 32°F + 32°F == does not compile

In the above case, if we assumed naïvely that we can add temperatures, the first sum would end up being 0°C, and the second one would be 64°F – clearly two different results.

You can add/subtract normal values to/from affine values:

    val temperatureIncreaseC = 5.of[celsius_deg]                            // equals +5°C
    val temperatureIncreaseF = temperatureIncreaseC.convert[fahrenheit_deg] // equals +9°F

    freezeC + temperatureIncreaseC  // equals 5°C
    freezeF + temperatureIncreaseF  // equals 41°F == 5°C
    freezeC - temperatureIncreaseC  // equals -5°C
    freezeF - temperatureIncreaseF  // equals 23°F == -5°C

You can also calculate a difference between two affine values, which yields a normal value:

    val boilC = 100.at[CelsiusScale] // equals 100°C == 212°F
    val diffC = boilC -- freezeC     // equals +100°C == +180°F

The operator name is double minus sign, because single minus sign was used for subtracting a normal value and Scala compiler cannot overload that method due to type erasure for value classes.

Defining affine spaces
----------------------

AffineSpace is defined as a pair containing a zero point and a unit.


    import stasiak.karol.units.Units._
    import stasiak.karol.units.DefiningUnits._

    type celsius_deg = DefineUnit[_deg~:_C]
    sealed trait CelsiusZero

    type CelsiusScale = DefineAffineSpace[CelsiusZero, celsius_deg]

Unlike normal values, affine spaces currently require conversion functions in both directions.

    implicit val fromCelsiusToFahrenheit = convertAffineSpace[CelsiusScale,FahrenheitScale]{ 
        x => x * (9/5.0) + 32  // (Double => Double)
    }
    implicit val fromFahrenheitToCelsius = convertAffineSpace[FahrenheitScale,CelsiusScale]{ 
        x => (x - 32) * (5/9.0)
    }

If two affine spaces only differ by their unit, you can use `changeUnit` method to convert between them:

    sealed trait UnixEpoch
    type UnixEpochSeconds = DefineAffineSpace[UnixEpoch, second]
    type UnixEpochMillis = DefineAffineSpace[UnixEpoch, millisecond]

    val timestamp = 123456789.at[UnixEpochSeconds] // equals 123456789 s from Unix Epoch
    timestamp.changeUnit[millisecond]              // equals 123456789000 ms from Unix Epoch

Affine values can be compared with relational operators, similarly to normal values.

Writing polymorphic functions using units of measure
----------------------------------------------------

Writing a function that works only on a specific units is trivial:

    def hypotenuse(a: DoubleU[metre], b: DoubleU[metre]) = (a.pow2 + b.pow2).sqrt

Writing a function that works on any units requires type constraint:

    def hypotenuse[U <: MUnit](a: DoubleU[U], b: DoubleU[U]) = (a.pow2 + b.pow2).sqrt

If you want to convert units inside your polymorphic function, you need an implicit conversion ratio:

    def hypotenuse[A <: MUnit, B <: MUnit](a: DoubleU[A], b: DoubleU[B])(implicit ratio: DoubleRatio[A,B]) = 
      (a.convert[B].pow2 + b.pow2).sqrt

Similarly with affine spaces:

    def tempDifference[A <: AffineSpace](a: DoubleA[T], b: DoubleA[T]) = a -- b // return type is DoubleU[T#Unit]

    def tempDifference[A1 <: AffineSpace, A2 <: AffineSpace](a: DoubleA[A1], b: DoubleA[A2])(
        implicit converter: DoubleAffineSpaceConverter[A1,A2]
        ) = a.convert[A2] -- b // return type is DoubleU[T2#Unit]

Other kinds of implicit parameters are required for automatic unit coercion during addition and pretty-printing.

Using arrays
------------

Sadly, Scala 2.10's arrays of value classes are boxed. As an alternative, `stasiak.karol.units.arrays` package provides classes `DoubleUArray[U]`, `DoubleAArray[A]`, `IntUArray[U]`, and `IntAArray[A]`.

Implementation details
======================

The implementation started with a pretty common type-level implementation of boolean and integers.

Type-level strings are either a cons of a char and a string, a single char, or an empty string. Each char is implemented currently as a pair of integers.

A basic unit is implemented as a pair of strings. One is an identifier that is used for type equality comparison, the other is used for implicit name generation. Current implementation of DefineUnit generates basic units with both those strings the same.

The main type for units (`MUnit`) is a linked list of pairs of basic units and integers, used to represent a type-level map.


TODO:
=====

* find why the second compilation crashes `fsc`; `sbt`'s incremental compiler for some reason works perfectly

* optimize compilation times

* add more units

* add more features

* write more documentation