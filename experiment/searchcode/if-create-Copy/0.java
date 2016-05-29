public static boolean isCreateParents(OpenOption... options) {
for (OpenOption option : options)
if (option == CreateOption.CREATE_PARENTS)
public static boolean isCreateParents(CopyOption... options) {
for (CopyOption option : options)
if (option == CreateOption.CREATE_PARENTS)

