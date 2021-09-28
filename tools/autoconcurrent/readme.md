Generates concurrent implementations of a class using hints provided in comments throughout the code.

    //CONCURRENT_CLASS_NAME TEXT override the default class name. Can be anywhere.
    //CONCURRENT_INLINE TEXT  will remove the comment in insert the text
    //CONCURRENT_ABOVE TEXT  will replace the line above with the text
    //CONCURRENT_BELOW TEXT  will replace the line below with the text
    //CONCURRENT_REMOVE_BELOW will remove the line below
    //CONCURRENT_REMOVE_ABOVE will remove the line above
    //CONCURRENT_REMOVE_LINE will remove the line it is placed on
    //CONCURRENT_MACRO NAME TEXT creates a macro that can be used instead of text
    //CONCURRENT_OMIT_BEGIN It will omit everything until it finds an OMIT_END
    //CONCURRENT_OMIT_END It will stop omitting when this is encountered.

A macro is identified by enclosing its name with brackets, e.g. {NAME}.