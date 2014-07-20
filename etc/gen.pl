use strict;
use JSON;

my $json = JSON->new->allow_nonref;

sub publish {
    my ($curmon, $curday, $data) = @_;

    my $text = $json->pretty->encode($data);
    my $dir = "events/$curmon";
    if (! -d "events") { mkdir("events"); }
    if (! -d $dir) { mkdir($dir); }

    open (J, ">$dir/$curday.json")
        || die "Could not write $dir/$curday.json : $!\n";
    print J $text;
    close(J);
}

sub mon2num {
    my ($name) = @_;
    my @months = (
        'January',
        'February',
        'March',
        'April',
        'May',
        'June',
        'July',
        'August',
        'September',
        'October',
        'November',
        'December'
        );

    for (my $idx = 0; $idx<= $#months; $idx++) {
        if ($name eq $months[$idx]) {
            return $idx;
        }
    }
    die "Unknown month: '$name'\n";
}

my $curmon = 0;
my $curday = 1;
my $content = undef;
my $cur_entry = undef;

while (<>) {
    chomp;

    s/^\s+//;
    s/\s+$//;

    my $line = $_;

    # Skip blank lines or boilerplate
    if ($line eq '') {
        next;
    }
    if ($line =~ /^<a href=\"http:\/\/feeds.feedburner.com\/TomMerrittcomTodayInTechHistory/) {
        next;
    }
    if ($line =~ /^Like Tech History\? Get Tom Merrit/) {
        next;
    }
    # New date.
    if ($line =~ /^(January|February|March|April|May|June|July|August|September|October|November|December)\s+(\d+)$/) {

        die "Bad state"  unless defined($curday);

        # add prior entry, then publish.
        if (defined($cur_entry)) {
            push(@$content, $cur_entry);
        }
        publish($curmon, $curday, $content);
        $curmon = mon2num($1);
        $curday = $2;
        # reset content
        $content = [];
        $cur_entry = undef;
        next;
    }

    # New entry.
    if ($line =~ /^(In )?(\d+)( BC)?\s*-\s*(.*)$/) {
        # add previous entry if needed.
        if (defined($cur_entry)) {
            push(@$content, $cur_entry);
        }
        $cur_entry = {};
        my $year;
        if (defined($3)) { $year = "$2$3"; }
        else { $year = "$2"; }
        $cur_entry->{'year'} = $year;
        $cur_entry->{'text'} = $4;
        $cur_entry->{'links'} = [];
        next;
    }

    # New link.
    if ($line =~ /^([\/\w]+(\s+[\/\w]+){0,3}:\s+)?(https?:\/\/.*)$/) {
        if (!defined($cur_entry)) {
            die "MISSING-ENTRY: $line\n";
        }
        push(@{$cur_entry->{'links'}}, $3);
        next;
    }

    die "UNKNOWN: $line\n";
}

# Add the last entries.
if (defined($cur_entry)) {
    push(@$content, $cur_entry);
}
publish($curmon, $curday, $content);
