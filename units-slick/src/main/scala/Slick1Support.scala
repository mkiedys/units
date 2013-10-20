/*
Copyright (c) 2013 Karol M. Stasiak

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package stasiak.karol.units

import stasiak.karol.units.SI._
import stasiak.karol.units.SI.Short._
import scala.slick.lifted._
import language.implicitConversions

package object slick1Support {

	implicit def implicit__intUTypeMapper[U<:MUnit] =
		MappedTypeMapper.base[IntU[U], Long](_.value, _.of[U])

	implicit def implicit__doubleUTypeMapper[U<:MUnit] =
		MappedTypeMapper.base[DoubleU[U], Double](_.value, _.of[U])

	implicit def implicit__intATypeMapper[A<:AffineSpace] =
		MappedTypeMapper.base[IntA[A], Long](_.value, _.at[A])

	implicit def implicit__doubleATypeMapper[A<:AffineSpace] =
		MappedTypeMapper.base[DoubleA[A], Double](_.value, _.at[A])
}