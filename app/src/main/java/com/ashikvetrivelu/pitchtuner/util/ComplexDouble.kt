package com.ashikvetrivelu.pitchtuner.util

import kotlin.math.*

/**
    Class to represent complex numbers as two "real" doubles
    Complex Number is given by Z = a + bi where a is the real part and b is the imaginary
 */
data class ComplexDouble(var real: Double, var imaginary: Double) {

    /**
        Returns the inverse square root of real and imaginary values.
        TODO: Use Quake Algorithm for faster abs value??
     */
    fun abs() = hypot(real, imaginary)


    /**
        Used for returning the phase angle between the plotted points obtained by calculating the roots of unity
        Mostly equal to 2*pi/N for N points in the unit circle identified by euler's theorem.
     */
    fun phase() = atan2(real, imaginary)

    fun plus(secondComplex: ComplexDouble) =
        ComplexDouble(real + secondComplex.real, imaginary + secondComplex.imaginary)

    fun minus(secondComplex: ComplexDouble) =
        ComplexDouble(real - secondComplex.real, imaginary - secondComplex.imaginary)

    /**
        Multiply imaginary numbers Z1 * Z2 = (a1 * a2 + a2 * b1 * i + b2 * a1 * i - b1 * b2)
        real part of product = a1 * a2 - b1 * b2
        imaginary part of product = a2*b1 + b2*a1
     */
    fun multiply(secondComplex: ComplexDouble) =
        ComplexDouble(real * secondComplex.real - imaginary * secondComplex.imaginary, real * secondComplex.imaginary + imaginary * secondComplex.real)

    fun scale(factor: Double) = ComplexDouble(factor * real, factor * imaginary)

    fun conjugate() = ComplexDouble(real, -imaginary)

    fun reciprocal() = ComplexDouble(real / (real * real + imaginary * imaginary), -imaginary / (real * real + imaginary * imaginary))

    fun divide(secondComplex: ComplexDouble) = multiply(secondComplex.reciprocal())

    /**
        Returns the exponential representation of complex number by Euler's Theorem
     */
    fun exponent() = ComplexDouble(exp(real) * cos(imaginary), exp(real) * sin(imaginary))

    fun sine() = ComplexDouble(sin(real) * cosh(imaginary), cos(real) * sinh(imaginary))

    fun cosine() = ComplexDouble(cos(real) * cosh(imaginary), -sin(real) * sinh(imaginary))

    fun tangent() = sine().divide(cosine())

    companion object {
        fun fromRealDouble(data: Double) = ComplexDouble(data, 0.0)

        fun fromRealDoubleArray(data: DoubleArray) = Array<ComplexDouble>(data.size) {
            ComplexDouble(data[it], 0.0)
        }

        fun toRealDoubleArray(data: Array<ComplexDouble>) = data.map { it.abs() }.toDoubleArray()
    }

}