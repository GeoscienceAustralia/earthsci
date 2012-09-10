There appears to be a bug in Eclipse where two plugin fragments, one ending in x86 and
one ending in x86_64, causes the native binaries not to load. Perhaps this is because
it thinks that the x86_64 folder is a version of the x86 folder?

For this reason, the 64-bit fragment directories are named x86__64 instead of x86_64.
