package com.zybooks.lotterychecker;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class TicketsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tickets, container, false);

        LotteryDB lotteryDB = LotteryDB.getInstance(getContext());
        EditText num1 = view.findViewById(R.id.number1);
        EditText num2 = view.findViewById(R.id.number2);
        EditText num3 = view.findViewById(R.id.number3);
        EditText num4 = view.findViewById(R.id.number4);
        EditText num5 = view.findViewById(R.id.number5);
        EditText powerMega = view.findViewById(R.id.power_mega_number);
        RadioGroup ticketType = view.findViewById(R.id.ticket_group);
        DatePicker startDate = view.findViewById(R.id.startDate);
        DatePicker endDate = view.findViewById(R.id.endDate);

        RecyclerView recyclerViewCurrent = view.findViewById(R.id.current_ticket_view);
        recyclerViewCurrent.setLayoutManager(new LinearLayoutManager(getActivity()));
        List<Ticket> tickets = lotteryDB.getTickets(true);
        TicketAdapter adapter = new TicketAdapter(tickets);
        recyclerViewCurrent.setAdapter(adapter);

        RecyclerView recyclerViewPast = view.findViewById(R.id.past_ticket_view);
        recyclerViewPast.setLayoutManager(new LinearLayoutManager(getActivity()));
        tickets = lotteryDB.getTickets(false);
        adapter = new TicketAdapter(tickets);
        recyclerViewPast.setAdapter(adapter);

        Button addTicketButton = view.findViewById(R.id.addTicket);
        addTicketButton.setOnClickListener(l -> {
            int number1 = Integer.parseInt(num1.getText().toString());
            int number2 = Integer.parseInt(num2.getText().toString());
            int number3 = Integer.parseInt(num3.getText().toString());
            int number4 = Integer.parseInt(num4.getText().toString());
            int number5 = Integer.parseInt(num5.getText().toString());
            int[] numbers = {number1, number2, number3, number4, number5};
            int pmBall = Integer.parseInt(powerMega.getText().toString());
            int checkedId = ticketType.getCheckedRadioButtonId();
            boolean mega = checkedId != R.id.powerballButton;
            int month = startDate.getMonth() + 1;
            int year = startDate.getYear();
            int day = startDate.getDayOfMonth();
            String sDate = formatDate(year, month, day);
            month = endDate.getMonth() + 1;
            year = endDate.getYear();
            day = endDate.getDayOfMonth();
            String eDate = formatDate(year, month, day);
            Ticket ticket = new Ticket(sDate, eDate, numbers, pmBall, mega);

            long result = lotteryDB.addTicket(ticket);
            if (result == -1) {
                Toast.makeText(getContext(), "Problem adding ticket", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Ticket added", Toast.LENGTH_LONG).show();
            }

        });

        Button deleteTicket = view.findViewById(R.id.deleteButton);
        deleteTicket.setOnClickListener(l -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Delete Ticket ID");

            View qtyView = inflater.inflate(R.layout.delete_ticket, null);
            builder.setView(qtyView);

            EditText idToDelete = qtyView.findViewById(R.id.delete_Id);

            builder.setPositiveButton("Delete", (dialogInterface, i) -> {
                int idInt = Integer.parseInt(String.valueOf(idToDelete.getText()));
                lotteryDB.deleteTicket(idInt);
            });

            builder.setNegativeButton("Cancel", null);

            builder.create();

            builder.show();
        });

        Button updateTickets = view.findViewById(R.id.update_tickets);
        updateTickets.setOnClickListener(l -> {
            List<Ticket> ticketList = lotteryDB.getTickets(true);
            for (Ticket ticket: ticketList) {
                lotteryDB.checkTicket(ticket);
            }
        });

        return view;
    }

    public String formatDate(int year, int month, int day) {
        if (month < 10) {
            if (day < 10) {
                return String.format(year + "-%02d-" + "%02d", month, day);
            } else {return String.format(year + "-%02d-" + day, month);}
        }

        if (day < 10) {return String.format(year + "-" + month + "-%02d", day);}
        return year + "-" + month + "-" + day;
    }

    private class TicketAdapter extends RecyclerView.Adapter<TicketHolder> {

        private final List<Ticket> ticketList;

        public TicketAdapter(List<Ticket> tickets) {
            ticketList = tickets;
        }

        @NonNull
        @Override
        public TicketHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new TicketHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(TicketHolder holder, int position) {
            Ticket ticket = ticketList.get(position);
            holder.bind(ticket);
            holder.itemView.setTag(ticket.getId());
        }

        @Override
        public int getItemCount() {
            return ticketList.size();
        }

        public void addTicket(Ticket ticket) {
            ticketList.add(ticket);
            notifyItemInserted(ticketList.size() - 1);
        }

        public void editTicket(Ticket original, Ticket edited) {
            int index = ticketList.indexOf(original);

            ticketList.add(index, edited);
            ticketList.remove(original);
            notifyItemChanged(index);
        }

        public void deleteTicket(Ticket ticket) {
            int index = ticketList.indexOf(ticket);
            ticketList.remove(ticket);
            notifyItemRemoved(index);
        }
    }

    private static class TicketHolder extends RecyclerView.ViewHolder {

        private final TextView mNameTextView;

        public TicketHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_ticket, parent, false));
            mNameTextView = itemView.findViewById(R.id.ticket_string);
        }

        public void bind(Ticket ticket) {
            mNameTextView.setText(ticket.toString());
        }
    }
}