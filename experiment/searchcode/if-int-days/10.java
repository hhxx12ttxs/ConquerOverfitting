int month = 3;

int year = 2001;

int days = 0;

switch (month) {

case (12):
days += 30;

case (11):
case (4):
days += 31;
case (3):
days += 28;
case (2):
days += 31;
case (1):
days += day;
}
if (((month > 2) &amp;&amp; ((year % 100 != 0) &amp;&amp; year % 4 == 0))

