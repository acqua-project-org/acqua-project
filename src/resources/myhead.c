/*
 My own version of head (it does not cut at line 74 as strangely head command does with large files). 
 compilation: 
   gcc myhead.c -o myhead
 usage: 
   myhead source_filename destination_filename lines_to_use_from_head
 example:
   myhead planetlab1.informatik.uni-wuerzburg.de-outputbak.txt testout.txt 128 && cat testout.txt | wc -l

*/
#include <stdio.h>
int main (int* argc, char** argv)
{
   FILE *filei = fopen ( argv[1], "r" );
   FILE *fileo = fopen ( argv[2], "w" );
   int maxlines = atoi( argv[3] );
   int linecounter = 0;
   printf("Printing %d lines...\n", maxlines);
   if ( filei != NULL )
   {
      char line [ 100000 ]; /* or other suitable maximum line size */

      while ( fgets ( line, sizeof line, filei ) != NULL ) /* read a line */
      {
         linecounter++;
         if (linecounter <= maxlines)
         {
            // fputs ( line, stdout ); /* write the line */
            fprintf(fileo, "%s", line);
         }
         else
         {
            break;
         }
      }
      fclose ( filei );
      fclose ( fileo );
   }
   else
   {
      printf("Cannot open the file '%s'.\n", argv[1]);
   }
   return 0;
}

