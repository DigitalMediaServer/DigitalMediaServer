/*
 * Digital Media Server, for streaming digital media to UPnP AV or DLNA
 * compatible devices based on PS3 Media Server and Universal Media Server.
 * Copyright (C) 2016 Digital Media Server developers.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses/.
 */
package net.pms.platform.macos;

import javax.annotation.Nonnull;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import net.pms.util.jna.StringByReference;

/**
 * JNA mapping of macOS' Objective-C Runtime.
 * <p>
 * Not all mappings are tested and some "guesses" have been made regarding
 * types, especially with regards to arrays. Some mappings might therefore
 * require some tweaking if they turn out to not work correctly.
 *
 * @see <a
 *      href="https://developer.apple.com/documentation/objectivec/objective_c_runtime?language=objc">Objective-C
 *      Runtime Reference</a>
 */
@SuppressWarnings({
	"checkstyle:MethodName",
	"checkstyle:ParameterName",
	"checkstyle:LineLength"
})
public interface ObjCRuntime extends Library {

	/**
	 * The instance of this interface.
	 */
	ObjCRuntime INSTANCE = Native.loadLibrary("objc.A", ObjCRuntime.class);


	/**************************
	 *  Working with Classes  *
	 **************************/


	/**
	 * Returns the name of a class.
	 *
	 * @param cls a class object.
	 * @return The name of the class, or the empty string if {@code cls} is
	 *         {@code null}.
	 */
	public String class_getName(Pointer cls);

	/**
	 * Returns the superclass of a class.
	 * <p>
	 * You should usually use NSObject‘s superclass method instead of this
	 * function.
	 *
	 * @param cls a class object.
	 * @return The superclass of the class, or {@code null} if {@code cls} is a
	 *         root class, or {@code null} if {@code cls} is {@code null}.
	 */
	public Pointer class_getSuperclass(Pointer cls);

	/**
	 * Returns a {@code boolean} value that indicates whether a class object is
	 * a metaclass.
	 *
	 * @param cls a class object.
	 * @return {@code true} if {@code cls} is a metaclass, {@code false} if
	 *         {@code cls} is a non-meta class, {@code false} if {@code cls} is
	 *         {@code null}.
	 */
	public boolean class_isMetaClass(Pointer cls);

	/**
	 * Returns the size of instances of a class.
	 *
	 * @param cls a class object.
	 * @return The size in bytes of instances of the class {@code cls}, or
	 *         {@code 0} if {@code cls} is {@code null}.
	 */
	public int class_getInstanceSize(Pointer cls);

	/**
	 * Returns the {@code Ivar} for a specified instance variable of a given
	 * class.
	 *
	 * @param cls the class whose instance variable you wish to obtain.
	 * @param name the name of the instance variable definition to obtain.
	 * @return A {@link Pointer} to an {@code Ivar} data structure containing
	 *         information about the instance variable specified by {@code name}.
	 */
	public Pointer class_getInstanceVariable(Pointer cls, String name);

	/**
	 * Returns the Ivar for a specified class variable of a given class.
	 *
	 * @param cls the class definition whose class variable you wish to obtain.
	 * @param name the name of the class variable definition to obtain.
	 * @return A {@link Pointer} to an {@code Ivar} data structure containing
	 *         information about the class variable specified by {@code name}.
	 */
	public Pointer class_getClassVariable(Pointer cls, String name);

	/**
	 * Adds a new instance variable to a class.
	 * <p>
	 * This function may only be called after
	 * {@link #objc_allocateClassPair(Pointer, String, long)} and before
	 * {@link #objc_registerClassPair(Pointer)}. Adding an instance variable to
	 * an existing class is not supported.
	 * <p>
	 * The class must not be a metaclass. Adding an instance variable to a
	 * metaclass is not supported.
	 * <p>
	 * The instance variable's minimum alignment in bytes is {@code 1 << align}.
	 * The minimum alignment of an instance variable depends on the ivar's type
	 * and the machine architecture. For variables of any pointer type, pass
	 * {@code log2(sizeof(pointer_type))}.
	 *
	 * @param cls the class object.
	 * @param name the variable name.
	 * @param size the variable size.
	 * @param alignment the alignment.
	 * @param types the types.
	 * @return {@code true} if the instance variable was added successfully,
	 *         otherwise {@code false} (for example, the class already contains
	 *         an instance variable with that name).
	 */
	public boolean class_addIvar(Pointer cls, String name, int size, byte alignment, String types);

	/**
	 * Returns a description of the {@code Ivar} layout for a given class.
	 *
	 * @param cls the class to inspect.
	 * @return A description of the {@code Ivar} layout for {@code cls}.
	 */
	public byte[] class_getIvarLayout(Pointer cls);

	/**
	 * Sets the {@code Ivar} layout for a given class.
	 *
	 * @param cls the class to modify.
	 * @param layout The layout of the {@code Ivars} for {@code cls}.
	 */
	public void class_setIvarLayout(Pointer cls, byte[] layout);

	/**
	 * Returns a description of the layout of weak {@code Ivars} for a given
	 * class.
	 *
	 * @param cls the class to inspect.
	 * @return A description of the layout of the weak {@code Ivars} for
	 *         {@code cls}.
	 */
	public byte[] class_getWeakIvarLayout(Pointer cls);

	/**
	 * Sets the layout for weak {@code Ivars} for a given class.
	 *
	 * @param cls the class to modify.
	 * @param layout the layout of the weak {@code Ivars} for {@code cls}.
	 */
	public void class_setWeakIvarLayout(Pointer cls, byte[] layout);

	/**
	 * Returns a property with a given name of a given class.
	 *
	 * @param cls a class object.
	 * @param name the name of the property.
	 * @return A {@link Pointer} of type {@code objc_property_t} describing the
	 *         property, or {@code null} if the class does not declare a
	 *         property with that name, or {@code null} if {@code cls} is
	 *         {@code null}.
	 */
	public Pointer class_getProperty(Pointer cls, String name);

	/**
	 * Adds a new method to a class with a given name and implementation.
	 * <p>
	 * class_addMethod will add an override of a superclass's implementation,
	 * but will not replace an existing implementation in this class. To change
	 * an existing implementation, use method_setImplementation.
	 *
	 * @param cls the class to which to add a method.
	 * @param name a selector that specifies the name of the method being added.
	 * @param imp a function which is the implementation of the new method. The
	 *            function must take at least two arguments — {@code self} and
	 *            {@code _cmd}.
	 * @param types An array of characters that describe the types of the
	 *            arguments to the method. For possible values, see <a href=
	 *            "https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/ObjCRuntimeGuide/Articles/ocrtTypeEncodings.html#//apple_ref/doc/uid/TP40008048-CH100"
	 *            >Objective-C Runtime Programming Guide &gt; Type
	 *            Encodings</a>. Since the function must take at least two
	 *            arguments — {@code self} and {@code _cmd}, the second and
	 *            third characters must be {@code "@:"} (the first character is
	 *            the return type).
	 * @return {@code true} if the method was added successfully, otherwise
	 *         {@code false} (for example, the class already contains a method
	 *         implementation with that name).
	 */
	public boolean class_addMethod(Pointer cls, Pointer name, Pointer imp, String types);

	/**
	 * Returns a specified instance method for a given class.
	 * <p>
	 * Note that this function searches superclasses for implementations,
	 * whereas {@code class_copyMethodList} does not.
	 *
	 * @param cls the class you want to inspect.
	 * @param selector the selector of the method you want to retrieve.
	 * @return The method that corresponds to the implementation of the selector
	 *         specified by {@code selector} for the class specified by
	 *         {@code cls}, or {@code null} if the specified class or its
	 *         superclasses do not contain an instance method with the specified
	 *         selector.
	 */
	public Pointer class_getInstanceMethod(Pointer cls, Pointer selector);

	/**
	 * Returns a {@link Pointer} to the data structure describing a given class
	 * method for a given class.
	 * <p>
	 * Note that this function searches superclasses for implementations,
	 * whereas {@code class_copyMethodList} does not.
	 *
	 * @param cls a pointer to a class definition. Pass the class that contains
	 *            the method you want to retrieve.
	 * @param selector a pointer of type {@code SEL}. Pass the selector of the
	 *            method you want to retrieve.
	 * @return A pointer to the Method data structure that corresponds to the
	 *         implementation of the selector specified by {@code selector} for
	 *         the class specified by {@code cls}, or {@code null} if the
	 *         specified class or its superclasses do not contain a class method
	 *         with the specified selector.
	 */
	public Pointer class_getClassMethod(Pointer cls, Pointer selector);

	/**
	 * Replaces the implementation of a method for a given class.
	 * <p>
	 * This function behaves in two different ways:
	 * <ul>
	 * <li>If the method identified by name does not yet exist, it is added as
	 * if class_addMethod were called. The type encoding specified by types is
	 * used as given.</li>
	 * <li>If the method identified by name does exist, its IMP is replaced as
	 * if method_setImplementation were called. The type encoding specified by
	 * types is ignored.</li>
	 * </ul>
	 *
	 * @param cls the class you want to modify.
	 * @param name a selector that identifies the method whose implementation
	 *            you want to replace.
	 * @param imp the new implementation for the method identified by name for
	 *            the class identified by {@code cls}.
	 * @param types An array of characters that describe the types of the
	 *            arguments to the method. For possible values, see <a href=
	 *            "https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/ObjCRuntimeGuide/Articles/ocrtTypeEncodings.html#//apple_ref/doc/uid/TP40008048-CH100"
	 *            >Objective-C Runtime Programming Guide &gt; Type
	 *            Encodings</a>. Since the function must take at least two
	 *            arguments — {@code self} and {@code _cmd}, the second and
	 *            third characters must be {@code "@:"} (the first character is
	 *            the return type).
	 * @return The previous implementation of the method identified by
	 *         {@code name} for the class identified by {@code cls}.
	 */
	public Pointer class_replaceMethod(Pointer cls, Pointer name, Pointer imp, String types);

	/**
	 * Returns the function pointer that would be called if a particular message
	 * were sent to an instance of a class.
	 * <p>
	 * {@code class_getMethodImplementation} may be faster than
	 * {@code method_getImplementation(class_getInstanceMethod(cls, name))}.
	 * <p>
	 * The function pointer returned may be a function internal to the runtime
	 * instead of an actual method implementation. For example, if instances of
	 * the class do not respond to the selector, the function pointer returned
	 * will be part of the runtime's message forwarding machinery.
	 *
	 * @param cls the class you want to inspect.
	 * @param name a selector.
	 * @return The function pointer that would be called if {@code name} were
	 *         called with an instance of the class, or {@code null} if
	 *         {@code cls} is {@code null}.
	 */
	public Pointer class_getMethodImplementation(Pointer cls, Pointer name);

	/**
	 * Returns the function pointer that would be called if a particular message
	 * were sent to an instance of a class.
	 *
	 * @param cls the class you want to inspect.
	 * @param name a selector.
	 * @return The function pointer that would be called if {@code name} were
	 *         called with an instance of the class, or {@code null} if
	 *         {@code cls} is {@code null}.
	 */
	public Pointer class_getMethodImplementation_stret(Pointer cls, Pointer name);

	/**
	 * Returns a {@code boolean} value that indicates whether instances of a
	 * class respond to a particular selector.
	 * <p>
	 * You should usually use {@code NSObject}'s {@code respondsToSelector:} or
	 * {@code instancesRespondToSelector:} methods instead of this function.
	 *
	 * @param cls the class you want to inspect.
	 * @param sel a selector.
	 * @return {@code true} if instances of the class respond to the selector,
	 *         otherwise {@code false}.
	 */
	public boolean class_respondsToSelector(Pointer cls, Pointer sel);

	/**
	 * Adds a protocol to a class.
	 *
	 * @param cls the class to modify.
	 * @param protocol the protocol to add to {@code cls}.
	 * @return {@code true} if the protocol was added successfully, otherwise
	 *         {@code false} (for example, the class already conforms to that
	 *         protocol).
	 */
	public boolean class_addProtocol(Pointer cls, Pointer protocol);

	/**
	 * Returns a {@code boolean} value that indicates whether a class conforms
	 * to a given protocol.
	 *
	 * @param cls the class you want to inspect.
	 * @param protocol the protocol.
	 * @return {@code true} if {@code cls} conforms to {@code protocol},
	 *         otherwise {@code false}.
	 */
	public boolean class_conformsToProtocol(Pointer cls, Pointer protocol);

	/**
	 * Returns the version number of a class definition.
	 * <p>
	 * You can use the version number of the class definition to provide
	 * versioning of the interface that your class represents to other classes.
	 * This is especially useful for object serialization (that is, archiving of
	 * the object in a flattened form), where it is important to recognize
	 * changes to the layout of the instance variables in different
	 * class-definition versions.
	 * <p>
	 * Classes derived from the Foundation framework {@code NSObject} class can
	 * obtain the class-definition version number using the {@code getVersion}
	 * class method, which is implemented using
	 * {@link #class_getVersion(Pointer)}.
	 *
	 * @param cls a {@link Pointer} to a {@code Class} data structure. Pass the
	 *            class definition for which you wish to obtain the version.
	 * @return An integer indicating the version number of the class definition.
	 */
	public int class_getVersion(Pointer cls);

	/**
	 * Sets the version number of a class definition.
	 *
	 * @param cls a pointer to a {@code Class} data structure. Pass the class
	 *            definition for which you wish to set the version.
	 * @param version an integer. Pass the new version number of the class
	 *            definition.
	 */
	public void class_setVersion(Pointer cls, int version);

	/**
	 * Used by CoreFoundation's toll-free bridging. Returns the id of the named
	 * class.
	 * <p>
	 * Do not call this function yourself.
	 *
	 * @param name The name of the {@code Class}.
	 * @return The id of the named class, or an uninitialized class structure
	 *         that will be used for the class when and if it does get loaded.
	 */
	public Pointer objc_getFutureClass(String name);

	/**
	 * Used by CoreFoundation's toll-free bridging.
	 * <p>
	 * Do not call this function yourself.
	 *
	 * @param cls Undocumented.
	 * @param name Undocumented.
	 */
	public void objc_setFutureClass(Pointer cls, String name);


	/********************
	 *  Adding Classes  *
	 ********************/


	/**
	 * Creates a new class and metaclass.
	 * <p>
	 * You can get a pointer to the new metaclass by calling
	 * {@code object_getClass(newClass)}.
	 * <p>
	 * To create a new class, start by calling
	 * {@link #objc_allocateClassPair(Pointer, String, long)}. Then set the
	 * class's attributes with functions like
	 * {@link #class_addMethod(Pointer, Pointer, Pointer, String)} and
	 * {@link #class_addIvar(Pointer, String, int, byte, String)}. When you are
	 * done building the class, call {@link #objc_registerClassPair(Pointer)}.
	 * The new class is now ready for use.
	 * <p>
	 * Instance methods and instance variables should be added to the class
	 * itself. Class methods should be added to the metaclass.
	 *
	 * @param superclass the class to use as the new class's superclass, or
	 *            {@code null} to create a new root class.
	 * @param name the string to use as the new class's name. The string will be
	 *            copied.
	 * @param extraBytes the number of bytes to allocate for indexed ivars at
	 *            the end of the class and metaclass objects. This should
	 *            usually be {@code 0}.
	 *
	 * @return The new class, or {@code null} if the class could not be created
	 *         (for example, the desired name is already in use).
	 *
	 */
	public Pointer objc_allocateClassPair(Pointer superclass, String name, long extraBytes);

	/**
	 * Destroy a class and its associated metaclass.
	 * <p>
	 * Do not call if instances of this class or a subclass exist.
	 *
	 * @param cls the class to be destroyed. It must have been allocated with
	 *            {@link #objc_allocateClassPair(Pointer, String, long)}.
	 */
	public void objc_disposeClassPair(Pointer cls);

	/**
	 * Registers a class that was allocated using
	 * {@link #objc_allocateClassPair(Pointer, String, long)}.
	 *
	 * @param cls the class you want to register.
	 */
	public void objc_registerClassPair(Pointer cls);

	/**
	 * Used by Foundation's Key-Value Observing.
	 * <p>
	 * Do not call this function yourself.
	 *
	 * @param original undocumented.
	 * @param name undocumented.
	 * @param extraBytes undocumented.
	 * @return undocumented.
	 */
	public Pointer objc_duplicateClass(Pointer original, String name, int extraBytes);


	/***************************
	 *  Instantiating Classes  *
	 ***************************/


	/**
	 * Creates an instance of a class, allocating memory for the class in the
	 * default malloc memory zone.
	 *
	 * @param cls the class that you wish to allocate an instance of.
	 * @param extraBytes an integer indicating the number of extra bytes to
	 *            allocate. The additional bytes can be used to store additional
	 *            instance variables beyond those defined in the class
	 *            definition.
	 * @return An instance of the class {@code cls}.
	 */
	public Pointer class_createInstance(Pointer cls, int extraBytes);

	/**
	 * Creates an instance of a class at the specific location provided.
	 *
	 * @param cls The class that you wish to allocate an instance of.
	 * @param bytes The location at which to allocate an instance of {@code cls}
	 *            . Must point to at least {@code class_getInstanceSize(cls)}
	 *            bytes of well-aligned, zero-filled memory.
	 * @return An instance of the class {@code cls} at bytes, if successful;
	 *         otherwise {@code null} (for example, if {@code cls} or
	 *         {@code bytes} are themselves {@code null}).
	 *
	 * @see #class_createInstance(Pointer, int)
	 * @since macOS 10.6
	 */
	public Pointer objc_constructInstance(Pointer cls, Memory bytes);

	/**
	 * Destroys an instance of a class without freeing memory and removes any
	 * associated references this instance might have had.
	 * <p>
	 * Core Foundation and other clients do call this under GC.
	 * <p>
	 * The garbage collector does not call this function. As a result, if you
	 * edit this function, you should also edit finalize. That said, Core
	 * Foundation and other clients do call this function under garbage
	 * collection.
	 *
	 * @param obj The class instance to destroy. Does nothing if {@code obj} is
	 *            {@code null}.
	 *
	 * @since macOS 10.6
	 */
	public void objc_destructInstance(Pointer obj);


	/****************************
	 *  Working with Instances  *
	 ****************************/


	/**
	 * Returns a copy of a given object.
	 *
	 * @param obj an Objective-C object.
	 * @param size the size of the object {@code obj}.
	 * @return A copy of {@code obj}.
	 */
	public Pointer object_copy(Pointer obj, int size);

	/**
	 * Frees the memory occupied by a given object.
	 *
	 * @param obj an Objective-C object.
	 * @return {@code null}
	 */
	public Pointer object_dispose(Pointer obj);

	/**
	 * Changes the value of an instance variable of a class instance.
	 * <p>
	 * Instance variables with known memory management (such as ARC strong and
	 * weak) use that memory management. Instance variables with unknown memory
	 * management are assigned as if they were {@code unsafe_unretained}.
	 *
	 * @param obj a {@link Pointer} to an instance of a class. Pass the object
	 *            containing the instance variable whose value you wish to
	 *            modify.
	 * @param name a {@link String}. Pass the name of the instance variable
	 *            whose value you wish to modify.
	 * @param value the new value for the instance variable.
	 * @return A pointer to the {@code Ivar} data structure that defines the
	 *         type and name of the instance variable specified by {@code name}.
	 */
	public Pointer object_setInstanceVariable(Pointer obj, String name, Pointer value);

	/**
	 * Obtains the value of an instance variable of a class instance.
	 *
	 * @param obj a pointer to an instance of a class. Pass the object
	 *            containing the instance variable whose value you wish to
	 *            obtain.
	 * @param name a {@link String}. Pass the name of the instance variable
	 *            whose value you wish to obtain.
	 * @param outValue on return, contains a {@link Pointer} to the value of the
	 *            instance variable.
	 * @return A {@link Pointer} to the {@code Ivar} data structure that defines
	 *         the type and name of the instance variable specified by
	 *         {@code name}.
	 */
	public Pointer object_getInstanceVariable(Pointer obj, String name, PointerByReference outValue);

	/**
	 * Returns a pointer to any extra bytes allocated with an instance given
	 * object.
	 * <p>
	 * This function returns a {@link Pointer} to any extra bytes allocated with
	 * the instance (as specified by {@link #class_createInstance(Pointer, int)}
	 * with {@code extraBytes > 0}). This memory follows the object's ordinary
	 * ivars, but may not be adjacent to the last ivar.
	 * <p>
	 * The returned {@link Pointer} is guaranteed to be pointer-size aligned,
	 * even if the area following the object's last ivar is less aligned than
	 * that. Alignment greater than pointer-size is never guaranteed, even if
	 * the area following the object's last ivar is more aligned than that.
	 * <p>
	 * In a garbage-collected environment, the memory is scanned conservatively.
	 *
	 * @param obj an Objective-C object.
	 * @return a {@link Pointer} to any extra bytes allocated with {@code obj}.
	 *         If {@code obj} was not allocated with any extra bytes, then
	 *         dereferencing the returned {@link Pointer} is undefined.
	 */
	public Pointer object_getIndexedIvars(Pointer obj);

	/**
	 * Reads the value of an instance variable in an object.
	 * <p>
	 * {@code object_getIvar} is faster than
	 * {@link #object_getInstanceVariable(Pointer, String, PointerByReference)}
	 * if the {@code Ivar} for the instance variable is already known.
	 *
	 * @param object the object containing the instance variable whose value you
	 *            want to read.
	 * @param ivar the {@code Ivar} describing the instance variable whose value
	 *            you want to read.
	 * @return The value of the instance variable specified by {@code ivar}, or
	 *         {@code null} if {@code object} is {@code null}.
	 *
	 * @since macOS 10.5
	 */
	public Pointer object_getIvar(Pointer object, Pointer ivar);

	/**
	 * Sets the value of an instance variable in an object.
	 * <p>
	 * Instance variables with known memory management (such as ARC strong and
	 * weak) use that memory management. Instance variables with unknown memory
	 * management are assigned as if they were unsafe_unretained.
	 * <p>
	 * {@code object_setIvar} is faster than
	 * {@link #object_setInstanceVariable(Pointer, String, Pointer)} if the
	 * {@code Ivar} for the instance variable is already known.
	 *
	 * @param object the object containing the instance variable whose value you
	 *            want to set.
	 * @param ivar the {@code Ivar} describing the instance variable whose value
	 *            you want to set.
	 * @param value The new value for the instance variable.
	 *
	 * @since macOS 10.5
	 */
	public void object_setIvar(Pointer object, Pointer ivar, Pointer value);

	/**
	 * Returns the class name of a given object.
	 *
	 * @param obj an Objective-C object.
	 * @return The name of the class of which {@code obj} is an instance.
	 */
	public String object_getClassName(Pointer obj);

	/**
	 * Returns the class of an object.
	 *
	 * @param object the object you want to inspect.
	 * @return The class object of which {@code object} is an instance, or
	 *         {@code null} if {@code object} is {@code null}.
	 *
	 * @since macOS 10.5
	 */
	public Pointer object_getClass(Pointer object);

	/**
	 * Sets the class of an object.
	 *
	 * @param object the object to modify.
	 * @param cls a class object.
	 * @return The previous value of {@code object}'s class, or {@code null} if
	 *         {@code object} is {@code null}.
	 *
	 * @since macOS 10.5
	 */
	public Pointer object_setClass(Pointer object, Pointer cls);


	/*********************************
	 *  Obtaining Class Definitions  *
	 *********************************/


	/**
	 * Obtains the list of registered class definitions.
	 * <p>
	 * The Objective-C runtime library automatically registers all the classes
	 * defined in your source code. You can create class definitions at runtime
	 * and register them with the {@code objc_addClass} function.
	 * <p>
	 * You cannot assume that class objects you get from this function are
	 * classes that inherit from {@code NSObject}, so you cannot safely call any
	 * methods on such classes without detecting that the method is implemented
	 * first.
	 *
	 * @param buffer an array of {@code Class} values. On output, each
	 *            {@code Class} value points to one class definition, up to
	 *            either {@code bufferCount} or the total number of registered
	 *            classes, whichever is less. You can pass {@code null} to
	 *            obtain the total number of registered class definitions
	 *            without actually retrieving any class definitions.
	 * @param bufferCount an integer value. Pass the number of pointers for
	 *            which you have allocated space in {@code buffer}. On return,
	 *            this function fills in only this number of elements. If this
	 *            number is less than the number of registered classes, this
	 *            function returns an arbitrary subset of the registered
	 *            classes.
	 * @return An integer value indicating the total number of registered
	 *         classes.
	 */
	public int objc_getClassList(Pointer buffer, int bufferCount);

	/**
	 * Returns the class definition of a specified class.
	 * <p>
	 * {@link #objc_getClass(String)} is different from this function in that if
	 * the class is not registered, {@link #objc_getClass(String)} calls the
	 * class handler callback and then checks a second time to see whether the
	 * class is registered. This function does not call the class handler
	 * callback.
	 *
	 * @param name the name of the class to look up.
	 * @return The {@code Class} object for the named class, or {@code null} if
	 *         the class is not registered with the Objective-C runtime.
	 */
	public Pointer objc_lookUpClass(String name);

	/**
	 * Returns the class definition of a specified class.
	 * <p>
	 * This is different from {@link #objc_lookUpClass(String)} in that if the
	 * class is not registered, this calls the class handler callback and then
	 * checks a second time to see whether the class is registered.
	 * {@link #objc_lookUpClass(String)} does not call the class handler
	 * callback.
	 * <p>
	 * Earlier implementations of this function (prior to OS X v10.0) terminate
	 * the program if the class does not exist.
	 *
	 * @param name the name of the class to look up.
	 * @return The {@code Class} object for the named class, or {@code null} if
	 *         the class is not registered with the Objective-C runtime.
	 */
	public Pointer objc_getClass(String name);

	/**
	 * Returns the class definition of a specified class.
	 * <p>
	 * This function is the same as {@link #objc_getClass(String)}, but kills
	 * the process if the class is not found.
	 * <p>
	 * This function is used by ZeroLink, where failing to find a class would be
	 * a compile-time link error without ZeroLink.
	 *
	 * @param name the name of the class to look up.
	 * @return The {@code Class} object for the named class.
	 */
	public Pointer objc_getRequiredClass(String name);

	/**
	 * Returns the metaclass definition of a specified class.
	 * <p>
	 * If the definition for the named class is not registered, this function
	 * calls the class handler callback and then checks a second time to see if
	 * the class is registered. However, every class definition must have a
	 * valid metaclass definition, and so the metaclass definition is always
	 * returned, whether it’s valid or not.
	 *
	 * @param name the name of the class to look up.
	 * @return The {@code Class} object for the metaclass of the named class, or
	 *         {@code null} if the class is not registered with the Objective-C
	 *         runtime.
	 */
	public Pointer objc_getMetaClass(String name);


	/*************************************
	 *  Working with Instance Variables  *
	 *************************************/


	/**
	 * Returns the name of an instance variable.
	 *
	 * @param ivar the instance variable you want to enquire about.
	 * @return A {@link String} containing the instance variable's name.
	 *
	 * @since macOS 10.5
	 */
	public String ivar_getName(Pointer ivar);

	/**
	 * Returns the type string of an instance variable.
	 * <p>
	 * For possible values, see <a href=
	 * "https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/ObjCRuntimeGuide/Articles/ocrtTypeEncodings.html#//apple_ref/doc/uid/TP40008048-CH100"
	 * >Objective-C Runtime Programming Guide &gt; Type Encodings</a>.
	 *
	 * @param ivar the instance variable you want to enquire about.
	 * @return A {@link String} containing the instance variable's type
	 *         encoding.
	 *
	 * @since macOS 10.5
	 */
	public String ivar_getTypeEncoding(Pointer ivar);

	/**
	 * Returns the offset of an instance variable.
	 * <p>
	 * For instance variables of type {@code id} or other object types, call
	 * {@link #object_getIvar(Pointer, Pointer)} and
	 * {@link #object_setIvar(Pointer, Pointer, Pointer)} instead of using this
	 * offset to access the instance variable data directly.
	 *
	 * @param ivar the instance variable you want to enquire about.
	 * @return The offset of {@code ivar}.
	 *
	 * @since macOS 10.5
	 */
	public long ivar_getOffset(Pointer ivar);


	/****************************
	 *  Associative References  *
	 ****************************/


	/**
	 * Sets an associated value for a given object using a given key and
	 * association policy.
	 *
	 * @param object the source object for the association.
	 * @param key the key for the association.
	 * @param value the value to associate with the key {@code key} for
	 *            {@code object}. Pass {@code null} to clear an existing
	 *            association.
	 * @param policy the policy for the association. For possible values, see <a
	 *            href=
	 *            "https://developer.apple.com/documentation/objectivec/objc_associationpolicy?language=objc"
	 *            >objc_AssociationPolicy</a>.
	 *
	 * @see #objc_setAssociatedObject(Pointer, Pointer, Pointer, Pointer)
	 * @see #objc_removeAssociatedObjects(Pointer)
	 */
	public void objc_setAssociatedObject(Pointer object, Pointer key, Pointer value, Pointer policy);

	/**
	 * Returns the value associated with a given object for a given key.
	 *
	 * @param object the source object for the association.
	 * @param key the key for the association.
	 * @return The value associated with the key {@code key} for {@code object}.
	 *
	 * @see #objc_setAssociatedObject(Pointer, Pointer, Pointer, Pointer)
	 */
	public Pointer objc_getAssociatedObject(Pointer object, String key);

	/**
	 * Removes all associations for a given object.
	 * <p>
	 * The main purpose of this function is to make it easy to return an object
	 * to a "pristine state”. You should not use this function for general
	 * removal of associations from objects, since it also removes associations
	 * that other clients may have added to the object. Typically you should use
	 * {@link #objc_setAssociatedObject(Pointer, Pointer, Pointer, Pointer)}
	 * with a {@code null} value to clear an association.
	 *
	 * @param object an object that maintains associated objects.
	 *
	 * @see #objc_setAssociatedObject(Pointer, Pointer, Pointer, Pointer)
	 * @see #objc_getAssociatedObject(Pointer, String)
	 */
	public void objc_removeAssociatedObjects(Pointer object);


	/**********************
	 *  Sending Messages  *
	 **********************/


	/**
	 * Sends a message with a simple return value to an instance of a class.
	 * <p>
	 * When it encounters a method call, the compiler generates a call to one of
	 * the functions {@link #objc_msgSend(Pointer, Pointer, Object...)},
	 * {@link #objc_msgSend_stret(Pointer, Pointer, Pointer, Object...)},
	 * {@link #objc_msgSendSuper(Pointer, Pointer, Object...)}, or
	 * {@link #objc_msgSendSuper_stret(Pointer, Pointer, Object...)}. Messages
	 * sent to an object’s superclass (using the {@code super} keyword) are sent
	 * using {@link #objc_msgSendSuper(Pointer, Pointer, Object...)}; other
	 * messages are sent using
	 * {@link #objc_msgSend(Pointer, Pointer, Object...)}. Methods that have
	 * data structures as return values are sent using
	 * {@link #objc_msgSendSuper_stret(Pointer, Pointer, Object...)} and
	 * {@link #objc_msgSend_stret(Pointer, Pointer, Pointer, Object...)}.
	 *
	 * @param self a {@link Pointer} to the instance of the class that is to
	 *            receive the message.
	 * @param op the selector of the method that handles the message.
	 * @param arguments a variable argument list containing the arguments to the
	 *            method.
	 * @return The return value of the method.
	 */
	public long objc_msgSend(Pointer self, Pointer op, Object... arguments);

	/**
	 * Sends a message with a floating-point return value to an instance of a
	 * class.
	 * <p>
	 * On the i386 platform, the ABI for functions returning a floating-point
	 * value is incompatible with that for functions returning an integral type.
	 * On the i386 platform, therefore, you must use
	 * {@link #objc_msgSend_fpret(Pointer, Pointer, Object...)} for functions
	 * returning non-integral type. For {@code float} or {@code long double}
	 * return types, cast the function to an appropriate function pointer type
	 * first.
	 * <p>
	 * This function is not used on the PPC or PPC64 platforms.
	 *
	 * @param self a {@link Pointer} to the instance of the class that is to
	 *            receive the message.
	 * @param op the selector of the method that handles the message.
	 * @param arguments a variable argument list containing the arguments to the
	 *            method.
	 * @return The return value of the method.
	 *
	 * @see #objc_msgSend(Pointer, Pointer, Object...)
	 * @since macOS 10.5
	 */
	public double objc_msgSend_fpret(Pointer self, Pointer op, Object... arguments);

	/**
	 * Sends a message with a data-structure return value to an instance of a
	 * class.
	 *
	 * @param stretAddr on input, a {@link Pointer} that points to a block of
	 *            memory large enough to contain the return value of the method.
	 *            On output, contains the return value of the method.
	 * @param self a {@link Pointer} to the instance of the class that is to
	 *            receive the message.
	 * @param op the selector of the method that handles the message.
	 * @param arguments a variable argument list containing the arguments to the
	 *            method.
	 * @return The return value of the method.
	 *
	 * @see #objc_msgSend(Pointer, Pointer, Object...)
	 */
	public Pointer objc_msgSend_stret(Pointer stretAddr, Pointer self, Pointer op, Object... arguments);

	/**
	 * Sends a message with a simple return value to the superclass of an
	 * instance of a class.
	 *
	 * @param superClassStruct a {@link Pointer} to an {@code objc_super} data
	 *            structure. Pass values identifying the context the message was
	 *            sent to, including the instance of the class that is to
	 *            receive the message and the superclass at which to start
	 *            searching for the method implementation.
	 * @param op the selector of the method that handles the message.
	 * @param arguments a variable argument list containing the arguments to the
	 *            method.
	 * @return The return value of the method identified by {@code op}.
	 *
	 * @see #objc_msgSend(Pointer, Pointer, Object...)
	 */
	public long objc_msgSendSuper(Pointer superClassStruct, Pointer op, Object... arguments);

	/**
	 * Sends a message with a data-structure return value to the superclass of
	 * an instance of a class.
	 *
	 * @param superClassStruct a {@link Pointer} to an {@code objc_super} data
	 *            structure. Pass values identifying the context the message was
	 *            sent to, including the instance of the class that is to
	 *            receive the message and the superclass at which to start
	 *            searching for the method implementation.
	 * @param op the selector of the method that handles the message.
	 * @param arguments a variable argument list containing the arguments to the
	 *            method.
	 * @return A {@link Pointer} to the return value of the method identified by
	 *         {@code op}.
	 *
	 * @see #objc_msgSendSuper(Pointer, Pointer, Object...)
	 */
	public Pointer objc_msgSendSuper_stret(Pointer superClassStruct, Pointer op, Object... arguments);


	/**************************
	 *  Working with Methods  *
	 **************************/


	/**
	 * Calls the implementation of a specified method.
	 * <p>
	 * Using this function to call the implementation of a method is faster than
	 * calling {@link #method_getImplementation(Pointer)} and
	 * {@link #method_getName(Pointer)}.
	 *
	 * @param receiver a pointer to the instance of the class that you want to
	 *            invoke the method on. This value must not be {@code null}.
	 * @param m the method whose implementation you want to call.
	 * @param arguments a variable argument list containing the arguments to the
	 *            method.
	 * @return The return value of the method.
	 *
	 * @since macOS 10.5
	 */
	public Pointer method_invoke(Pointer receiver, Pointer m, Object... arguments);

	/**
	 * Calls the implementation of a specified method that returns a
	 * data-structure.
	 * <p>
	 * Using this function to call the implementation of a method is faster than
	 * calling {@link #method_getImplementation(Pointer)} and
	 * {@link #method_getName(Pointer)}.
	 *
	 * @param receiver a pointer to the instance of the class that you want to
	 *            invoke the method on. This value must not be {@code null}.
	 * @param m the method whose implementation you want to call.
	 * @param arguments a variable argument list containing the arguments to the
	 *            method.
	 * @return The return value of the method.
	 *
	 * @since macOS 10.5
	 */
	public Pointer method_invoke_stret(Pointer receiver, Pointer m, Object... arguments);

	/**
	 * Returns the name of a method.
	 * <p>
	 * To get the method name as a {@link String}, call
	 * {@code sel_getName(method_getName(method))}.
	 *
	 * @param method The method to inspect.
	 * @return A {@link Pointer} of type {@code SEL}.
	 *
	 * @since macOS 10.5
	 */
	public Pointer method_getName(Pointer method);

	/**
	 * Returns the implementation of a method.
	 *
	 * @param method the method to inspect.
	 * @return A function {@link Pointer} of type {@code IMP}.
	 *
	 * @since macOS 10.5
	 */
	public Pointer method_getImplementation(Pointer method);

	/**
	 * Returns a string describing a method's parameter and return types.
	 *
	 * @param method the method to inspect.
	 * @return A {@link String}. The string may be {@code null}.
	 *
	 * @since macOS 10.5
	 */
	public String method_getTypeEncoding(Pointer method);

	/**
	 * Returns a string describing a method's return type.
	 *
	 * @param method the method to inspect.
	 * @return A {@link String} describing the return type. You must free the
	 *         string with {@code free()}.
	 *
	 * @since macOS 10.5
	 */
	public String method_copyReturnType(Pointer method);

	/**
	 * Returns a string describing a single parameter type of a method.
	 *
	 * @param method the method to inspect.
	 * @param index the index of the parameter to inspect.
	 *
	 * @return A {@link String} describing the type of the parameter at index
	 *         {@code index}, or {@code null} if method has no parameter index
	 *         {@code index}. You must free the string with {@code free()}.
	 *
	 * @since macOS 10.5
	 */
	public String method_copyArgumentType(Pointer method, int index);

	/**
	 * Returns by reference a string describing a method's return type.
	 * <p>
	 * The method's return type string is copied to {@code dst}. {@code dst} is
	 * filled as if {@code strncpy(dst, parameter_type, dst_len)} were called.
	 *
	 * @param method the method you want to inquire about.
	 * @param dst the reference string to store the description.
	 * @param dst_len the maximum number of characters that can be stored in
	 *            {@code dst}.
	 *
	 * @since macOS 10.5
	 */
	public void method_getReturnType(Pointer method, StringByReference dst, long dst_len);

	/**
	 * Returns the number of arguments accepted by a method.
	 *
	 * @param method a {@link Pointer} to a {@code Method} data structure. Pass
	 *            the method in question.
	 * @return The number of arguments accepted by the given method.
	 */
	public int method_getNumberOfArguments(Pointer method);

	/**
	 * Returns by reference a string describing a single parameter type of a
	 * method.
	 * <p>
	 * The parameter type string is copied to {@code dst}. {@code dst} is filled
	 * as if {@code strncpy(dst, parameter_type, dst_len)} were called. If the
	 * method contains no parameter with that index, {@code dst} is filled as if
	 * {@code strncpy(dst, "", dst_len)} were called.
	 *
	 * @param method the method you want to inquire about.
	 * @param index the index of the parameter you want to inquire about.
	 * @param dst the reference string to store the description.
	 * @param dst_len the maximum number of characters that can be stored in
	 *            {@code dst}.
	 *
	 * @since macOS 10.5
	 */
	public void method_getArgumentType(Pointer method, int index, StringByReference dst, long dst_len);

	/**
	 * Sets the implementation of a method.
	 *
	 * @param method the method for which to set an implementation.
	 * @param imp the implementation to set to this method.
	 * @return The previous implementation of the method.
	 *
	 * @since macOS 10.5
	 */
	public Pointer method_setImplementation(Pointer method, Pointer imp);

	/**
	 * Exchanges the implementations of two methods.
	 * <p>
	 * This is an atomic version of the following: <code><pre>
	 *  IMP imp1 = method_getImplementation(m1);
	 *  IMP imp2 = method_getImplementation(m2);
	 *  method_setImplementation(m1, imp2);
	 *  method_setImplementation(m2, imp1);
	 * </pre></code>
	 *
	 * @param m1 the method to exchange with second method.
	 * @param m2 the method to exchange with first method.
	 *
	 * @since macOS 10.5
	 */
	public void method_exchangeImplementations(Pointer m1, Pointer m2);


	/****************************
	 *  Working with Libraries  *
	 ****************************/


	/**
	 * Returns the names of all the loaded Objective-C frameworks and dynamic
	 * libraries.
	 *
	 * @param outCount the number of names returned.
	 * @return An array of C strings of names. Must be {@code free()}'d by
	 *         caller.
	 *
	 * @since macOS 10.5
	 */
	@Nonnull
	public Pointer objc_copyImageNames(IntByReference outCount);

	/**
	 * Returns the dynamic library name a class originated from.
	 *
	 * @param cls the class you are inquiring about.
	 * @return The name of the library containing this class.
	 *
	 * @since macOS 10.5
	 */
	public String class_getImageName(Pointer cls);

	/**
	 * Returns the names of all the classes within a library.
	 *
	 * @param image the library or framework you are inquiring about.
	 * @param outCount the number of class names returned.
	 * @return An array of C strings representing the class names.
	 *
	 * @since macOS 10.5
	 */
	public Pointer objc_copyClassNamesForImage(String image, IntByReference outCount);


	/****************************
	 *  Working with Selectors  *
	 ****************************/


	/**
	 * Returns the name of the method specified by a given selector.
	 *
	 * @param sel a {@link Pointer} of type {@code SEL}. Pass the selector whose
	 *            name you wish to determine.
	 * @return A {@link String} indicating the name of the selector.
	 */
	public String sel_getName(Pointer sel);

	/**
	 * Registers a method with the Objective-C runtime system, maps the method
	 * name to a selector, and returns the selector value.
	 * <p>
	 * You must register a method name with the Objective-C runtime system to
	 * obtain the method’s selector before you can add the method to a class
	 * definition. If the method name has already been registered, this function
	 * simply returns the selector.
	 *
	 * @param name a {@link String}. Pass the name of the method you wish to
	 *            register.
	 * @return A {@link Pointer} of type {@code SEL} specifying the selector for
	 *         the named method.
	 */
	public Pointer sel_registerName(String name);

	/**
	 * Registers a method name with the Objective-C runtime system.
	 * <p>
	 * The implementation of this method is identical to the implementation of
	 * {@link #sel_registerName(String)}.
	 * <p>
	 * Prior to OS X version 10.0, this method tried to find the selector mapped
	 * to the given name and returned {@code null} if the selector was not
	 * found. This was changed for safety, because it was observed that many of
	 * the callers of this function did not check the return value for
	 * {@code null}.
	 *
	 * @param name a {@link String}. Pass the name of the method you wish to
	 *            register.
	 * @return A {@link Pointer} of type {@code SEL} specifying the selector for
	 *         the named method.
	 */
	public Pointer sel_getUid(String name);

	/**
	 * Returns a {@code boolean} value that indicates whether two selectors are
	 * equal.
	 * <p>
	 * {@code sel_isEqual} is equivalent to {@code ==}.
	 *
	 * @param lhs the selector to compare with {@code rhs}.
	 * @param rhs the selector to compare with {@code lhs}.
	 *
	 * @return {@code true} if {@code lhs} and {@code rhs} are equal, otherwise
	 *         {@code false}.
	 *
	 * @since macOS 10.5
	 */
	public boolean sel_isEqual(Pointer lhs, Pointer rhs);


	/****************************
	 *  Working with Protocols  *
	 ****************************/


	/**
	 * Returns a specified protocol.
	 * <p>
	 * This function acquires the runtime lock.
	 *
	 * @param name the name of a protocol.
	 * @return The protocol named {@code name}, or {@code null} if no protocol
	 *         named {@code name} could be found.
	 *
	 * @since macOS 10.5
	 */
	public Pointer objc_getProtocol(String name);

	/**
	 * Returns an array of all the protocols known to the runtime.
	 * <p>
	 * This function acquires the runtime lock.
	 *
	 * @param outCount upon return, contains the number of protocols in the
	 *            returned array.
	 * @return An array of all the protocols known to the runtime. The array
	 *         contains {@code outCount} pointers followed by a {@code null}
	 *         terminator. You must free the list with {@code free()}.
	 *
	 * @since macOS 10.5
	 */
	public Pointer[] objc_copyProtocolList(IntByReference outCount);

	/**
	 * Creates a new protocol instance that cannot be used until registered with
	 * {@link #objc_registerProtocol(Pointer)}.
	 * <p>
	 * There is no dispose method for this.
	 *
	 * @param name the name of the protocol to create.
	 * @return The Protocol instance on success, {@code null} if a protocol with
	 *         the same name already exists.
	 *
	 * @since macOS 10.7
	 */
	public Pointer objc_allocateProtocol(String name);

	/**
	 * Registers a newly constructed protocol with the runtime. The protocol
	 * will be ready for use and is immutable after this.
	 *
	 * @param proto the protocol you want to register.
	 *
	 * @since macOS 10.7
	 */
	public void objc_registerProtocol(Pointer proto);

	/**
	 * Adds a method to a protocol. The protocol must be under construction.
	 *
	 * @param proto the protocol to add a method to.
	 * @param name the name of the method to add.
	 * @param types a {@link String} that represents the method signature.
	 * @param isRequiredMethod {@code true} if the method is not an optional
	 *            method.
	 * @param isInstanceMethod {@code true} if the method is an instance method.
	 *
	 * @since macOS 10.7
	 */
	public void protocol_addMethodDescription(
		Pointer proto,
		Pointer name,
		String types,
		boolean isRequiredMethod,
		boolean isInstanceMethod
	);

	/**
	 * Adds an incorporated protocol to another protocol. The protocol being
	 * added to must still be under construction, while the additional protocol
	 * must be already constructed.
	 *
	 * @param proto the protocol you want to add to, it must be under
	 *            construction.
	 * @param addition the protocol you want to incorporate into {@code proto},
	 *            it must be registered.
	 *
	 * @since macOS 10.7
	 */
	public void protocol_addProtocol(Pointer proto, Pointer addition);

	/**
	 * Adds a property to a protocol. The protocol must be under construction.
	 *
	 * @param proto the protocol to add a property to.
	 * @param name the name of the property.
	 * @param attributes an array of property attributes.
	 * @param attributeCount the number of attributes in {@code attributes}.
	 * @param isRequiredProperty {@code true} if the property (accessor methods)
	 *            is not optional.
	 * @param isInstanceProperty {@code true} if the property (accessor methods)
	 *            are instance methods. This is the only case allowed for a
	 *            property, as a result, setting this to {@code false} will not
	 *            add the property to the protocol at all.
	 *
	 * @since macOS 10.7
	 */
	public void protocol_addProperty(
		Pointer proto,
		String name,
		Pointer attributes,
		int attributeCount,
		boolean isRequiredProperty,
		boolean isInstanceProperty
	);

	/**
	 * Returns the name of a protocol.
	 *
	 * @param protocol a protocol.
	 * @return The name of the protocol {@code proto} as a {@link String}.
	 *
	 * @since macOS 10.5
	 */
	public String protocol_getName(Pointer protocol);

	/**
	 * Returns the name of a protocol.
	 *
	 * @param protocol a protocol.
	 * @param other a protocol.
	 * @return The name of the protocol {@code protocol} as a {@link String}.
	 *
	 * @since macOS 10.5
	 */
	public boolean protocol_isEqual(Pointer protocol, Pointer other);

	/**
	 * Returns an array of method descriptions of methods meeting a given
	 * specification for a given protocol.
	 * <p>
	 * Methods in other protocols adopted by this protocol are not included.
	 *
	 * @param protocol a protocol.
	 * @param isRequiredMethod a {@code boolean} value that indicates whether
	 *            returned methods should be required methods (pass {@code true}
	 *            to specify required methods).
	 * @param isInstanceMethod a {@code boolean} value that indicates whether
	 *            returned methods should be instance methods (pass {@code true}
	 *            to specify instance methods).
	 * @param outCount upon return, contains the number of method description
	 *            structures in the returned array.
	 * @return A C array of {@code objc_method_description} structures
	 *         containing the names and types of {@code protocol}'s methods
	 *         specified by {@code isRequiredMethod} and
	 *         {@code isInstanceMethod}. The array contains {@code outCount}
	 *         pointers followed by a {@code null} terminator. You must free the
	 *         list with {@code free()}. If the protocol declares no methods
	 *         that meet the specification, {@code null} is returned and
	 *         {@code outCount} is {@code 0}.
	 *
	 * @since macOS 10.5
	 */
	public Pointer protocol_copyMethodDescriptionList(
		Pointer protocol,
		boolean isRequiredMethod,
		boolean isInstanceMethod,
		IntByReference outCount
	);

	/**
	 * Returns a method description structure for a specified method of a given
	 * protocol.
	 * <p>
	 * This function recursively searches any protocols that this protocol
	 * conforms to.
	 *
	 * @param protocol a protocol.
	 * @param aSel a selector.
	 * @param isRequiredMethod a {@code boolean} value that indicates whether
	 *            {@code aSel} is a required method.
	 * @param isInstanceMethod a {@code boolean} value that indicates whether
	 *            {@code aSel} is an instance method.
	 *
	 * @return An {@code objc_method_description} structure that describes the
	 *         method specified by {@code aSel}, {@code isRequiredMethod}, and
	 *         {@code isInstanceMethod} for the protocol {@code protocol}. If
	 *         the protocol does not contain the specified method, returns an
	 *         {@code objc_method_description} structure with the value {@code
	 *         null, null}}.
	 *
	 * @since macOS 10.5
	 */
	public Pointer protocol_getMethodDescription(
		Pointer protocol,
		Pointer aSel,
		boolean isRequiredMethod,
		boolean isInstanceMethod
	);

	/**
	 * Returns an array of the required instance properties declared by a
	 * protocol.
	 * <p>
	 * Identical to
	 * {@code protocol_copyPropertyList2(proto, outCount, true, true)}
	 *
	 * @param proto a protocol.
	 * @param outCount upon return, contains the number of elements in the
	 *            returned array.
	 * @return A C array of pointers of type {@code objc_property_t} describing
	 *         the properties declared by {@code proto}. Any properties declared
	 *         by other protocols adopted by this protocol are not included. The
	 *         array contains {@code outCount} pointers followed by a
	 *         {@code null} terminator. You must free the array with
	 *         {@code free()}. If the protocol declares no matching properties,
	 *         {@code null} is returned and {@code outCount} is {@code 0}.
	 *
	 * @since macOS 10.5
	 */
	public Pointer protocol_copyPropertyList(Pointer proto, IntByReference outCount);

	/**
	 * Returns an array of properties declared by a protocol.
	 *
	 * @param proto a protocol.
	 * @param outCount upon return, contains the number of elements in the
	 *            returned array.
	 * @param isRequiredProperty {@code true} returns required properties,
	 *            {@code false} returns optional properties.
	 * @param isInstanceProperty {@code true} returns instance properties,
	 *            {@code false} returns class properties.
	 * @return A C array of pointers of type {@code objc_property_t} describing
	 *         the properties declared by {@code proto}. Any properties declared
	 *         by other protocols adopted by this protocol are not included. The
	 *         array contains {@code outCount} pointers followed by a
	 *         {@code null} terminator. You must free the array with
	 *         {@code free()}. If the protocol declares no matching properties,
	 *         {@code null} is returned and {@code outCount} is {@code 0}.
	 *
	 * @since macOS 10.12
	 */
	public Pointer protocol_copyPropertyList2(
		Pointer proto,
		IntByReference outCount,
		boolean isRequiredProperty,
		boolean isInstanceProperty
	);

	/**
	 * Returns the specified property of a given protocol.
	 *
	 * @param proto a protocol.
	 * @param name the name of a property.
	 * @param isRequiredProperty {@code true} searches for a required property,
	 *            {@code false} searches for an optional property.
	 * @param isInstanceProperty {@code true} searches for an instance property,
	 *            {@code false} searches for a class property.
	 * @return The property specified by {@code name},
	 *         {@code isRequiredProperty}, and {@code isInstanceProperty} for
	 *         {@code proto}, or {@code null} if none of {@code proto}'s
	 *         properties meet the specification.
	 *
	 * @since macOS 10.5
	 */
	public Pointer protocol_getProperty(
		Pointer proto,
		String name,
		boolean isRequiredProperty,
		boolean isInstanceProperty
	);

	/**
	 * Returns an array of the protocols adopted by a protocol.
	 *
	 * @param proto a protocol.
	 * @param outCount upon return, contains the number of elements in the
	 *            returned array.
	 * @return A C array of protocols adopted by {@code proto}. The array
	 *         contains {@code outCount} pointers followed by a {@code null}
	 *         terminator. You must free the array with {@code free()}. If the
	 *         protocol declares no properties, {@code null} is returned and
	 *         {@code outCount} is {@code 0}.
	 *
	 * @since macOS 10.5
	 */
	public Pointer protocol_copyProtocolList(Pointer proto, IntByReference outCount);

	/**
	 * Returns a Boolean value that indicates whether one protocol conforms to
	 * another protocol.
	 * <p>
	 * One protocol can incorporate other protocols using the same syntax that
	 * classes use to adopt a protocol:
	 * <p>
	 * {@code @protocol ProtocolName < protocol list >}
	 * <p>
	 * All the protocols listed between angle brackets are considered part of
	 * the {@code ProtocolName} protocol.
	 *
	 * @param proto a protocol.
	 * @param other a protocol.
	 * @return {@code true} if {@code proto} conforms to {@code other},
	 *         otherwise {@code false}.
	 *
	 * @since macOS 10.5
	 */
	public boolean protocol_conformsToProtocol(Pointer proto, Pointer other);


	/*****************************
	 *  Working with Properties  *
	 *****************************/


	/**
	 * Returns the name of a property.
	 *
	 * @param property The property you want to inquire about.
	 * @return A {@link String} containing the property's name.
	 *
	 * @since macOS 10.5
	 */
	public String property_getName(Pointer property);

	/**
	 * Returns the attribute string of a property.
	 * <p>
	 * The format of the attribute string is described in <a href=
	 * "https://developer.apple.com/library/archive/documentation/Cocoa/Conceptual/ObjCRuntimeGuide/Articles/ocrtPropertyIntrospection.html#//apple_ref/doc/uid/TP40008048-CH101">
	 * Declared Properties in Objective-C Runtime Programming Guide</a>.
	 *
	 * @param property a property.
	 * @return A {@link String} containing the property's attributes.
	 *
	 * @since macOS 10.5
	 */
	public String property_getAttributes(Pointer property);

	/**
	 * Returns the value of a property attribute given the attribute name.
	 *
	 * @param property the property whose attribute value you are interested in.
	 * @param attributeName the {@link String} representing the attribute name.
	 * @return The value {@link String} of the attribute {@code attributeName}
	 *         if it exists in {@code property}, {@code null} otherwise.
	 *
	 * @since macOS 10.7
	 */
	public String property_copyAttributeValue(Pointer property, String attributeName);

	/**
	 * Returns an array of property attributes for a property.
	 *
	 * @param property the property whose attributes you want copied.
	 * @param outCount the number of attributes returned in the array.
	 * @return A C array of property attributes; must be {@code free()}'d by the
	 *         caller.
	 *
	 * @since macOS 10.7
	 */
	public Pointer property_copyAttributeList(Pointer property, IntByReference outCount);


	/***********************************
	 *  Objective-C Language Features  *
	 ***********************************/


	/**
	 * This function is inserted by the compiler when a mutation is detected
	 * during a foreach iteration. It gets called when a mutation occurs, and
	 * the {@code enumerationMutationHandler} is enacted if it is set up. A
	 * fatal error occurs if a handler is not set up.
	 *
	 * @param obj the object being mutated.
	 *
	 * @since macOS 10.5
	 */
	public void objc_enumerationMutation(Pointer obj);

	/**
	 * Sets the current mutation handler.
	 *
	 * @param handler the function pointer to the new mutation handler.
	 *
	 * @since macOS 10.5
	 */
	public void objc_setEnumerationMutationHandler(Pointer handler);

	/**
	 * Set the function to be called by {@code objc_msgForward}. See also
	 * {@code message.h::_objc_msgForward}.
	 *
	 * @param fwd the function to be jumped to by {@code objc_msgForward}.
	 * @param fwd_stret the function to be jumped to by
	 *            {@code objc_msgForward_stret}.
	 *
	 * @since macOS 10.5
	 */
	public void objc_setForwardHandler(Pointer fwd, Pointer fwd_stret);

	/**
	 * Creates a pointer to a function that will call the block when the method
	 * is called.
	 *
	 * @param block the block that implements this method. Its signature should
	 *            be: {@code method_return_type ^(id self, method_args...)}. The
	 *            selector is not available as a parameter to this block. The
	 *            block is copied with {@code Block_copy()}.
	 * @return The {@link Pointer} that calls this block. Must be disposed of
	 *         with {@code imp_removeBlock}.
	 *
	 * @since macOS 10.7
	 */
	public Pointer imp_implementationWithBlock(Pointer block);

	/**
	 * Return the block associated with an {@code IMP} that was created using
	 * {@link #imp_implementationWithBlock(Pointer)}.
	 *
	 * @param anImp the {@code IMP} that calls this block.
	 * @return The block called by {@code anImp}.
	 *
	 * @since macOS 10.7
	 */
	public Pointer imp_getBlock(Pointer anImp);

	/**
	 * Disassociates a block from an {@code IMP} that was created using
	 * {@link #imp_implementationWithBlock(Pointer)} and releases the copy of
	 * the block that was created.
	 *
	 * @param anImp an {@code IMP} that was created using
	 *            {@link #imp_implementationWithBlock(Pointer)}.
	 * @return {@code true} if the block was released successfully,
	 *         {@code false} otherwise. (For example, the block might not have
	 *         been used to create an {@code IMP} previously).
	 *
	 * @since macOS 10.7
	 */
	public boolean imp_removeBlock(Pointer anImp);

	/**
	 * This loads the object referenced by a weak pointer and returns it, after
	 * retaining and auto-releasing the object to ensure that it stays alive
	 * long enough for the caller to use it. This function would be used
	 * anywhere a {@code __weak} variable is used in an expression.
	 *
	 * @param location the weak pointer address.
	 * @return The object pointed to by {@code location}, or {@code null} if
	 *         {@code location} is {@code null}.
	 *
	 * @since macOS 10.7
	 */
	public Pointer objc_loadWeak(PointerByReference location);

	/**
	 * This function stores a new value into a {@code __weak} variable. It would
	 * be used anywhere a {@code __weak} variable is the target of an
	 * assignment.
	 *
	 * @param location the address of the weak pointer itself.
	 * @param obj the new object this weak pointer should now point to.
	 * @return The value stored into {@code location}, i.e. {@code obj}.
	 *
	 * @since macOS 10.7
	 */
	public Pointer objc_storeWeak(PointerByReference location, Pointer obj);
}
